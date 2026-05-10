package com.quietchatter.talk.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.quietchatter.talk.application.`in`.*
import com.quietchatter.talk.application.out.*
import com.quietchatter.talk.domain.ReactionType
import com.quietchatter.talk.domain.Talk
import com.quietchatter.talk.domain.TalkOwnershipService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class TalkService(
    private val talkPersistable: TalkPersistable,
    private val talkLoadable: TalkLoadable,
    private val reactionLoadable: ReactionLoadable,
    private val outboxEventPersistable: OutboxEventPersistable,
    private val memberLoadable: MemberLoadable,
    private val objectMapper: ObjectMapper,
    private val talkOwnershipService: TalkOwnershipService
) : TalkCommandable, TalkQueryable {

    @Transactional
    override fun createTalk(command: CreateTalkCommand): UUID {
        val nickname = memberLoadable.getMemberNickname(command.memberId)
        
        val talk = Talk(
            bookId = command.bookId,
            memberId = command.memberId,
            nickname = nickname,
            content = command.content,
            dateToHidden = command.dateToHidden
        )
        return talkPersistable.save(talk).id!!
    }

    @Transactional
    override fun updateTalk(command: UpdateTalkCommand) {
        val talk = getTalkOrThrow(command.talkId)
        talkOwnershipService.updateContentByOwner(talk, command.memberId, command.content)
        talkPersistable.save(talk)
    }

    @Transactional
    override fun deleteTalk(command: DeleteTalkCommand) {
        val talk = getTalkOrThrow(command.talkId)
        talkOwnershipService.hideByOwner(talk, command.memberId)
        talkPersistable.save(talk)
    }

    @Transactional
    override fun restoreTalk(command: RestoreTalkCommand) {
        val talk = talkLoadable.findById(command.talkId)
            ?: throw IllegalArgumentException("Talk not found: ${command.talkId}")
        talkOwnershipService.restoreByOwner(talk, command.memberId)
        talkPersistable.save(talk)
    }

    @Transactional
    override fun hideAllByMember(memberId: UUID) {
        talkPersistable.hideAllByMemberId(memberId)
    }

    @Transactional
    override fun hideExpiredTalks(): Int {
        val now = java.time.LocalDate.now()
        val expiredTalks = talkPersistable.findExpiredTalks(now)
        
        expiredTalks.forEach { talk ->
            talkOwnershipService.hideBySystem(talk)
            talkPersistable.save(talk)
            
            val outboxEvent = com.quietchatter.talk.adaptor.out.outbox.OutboxEvent(
                aggregateType = "Talk",
                aggregateId = talk.id.toString(),
                type = "TalkHiddenEvent",
                payload = objectMapper.writeValueAsString(mapOf("talkId" to talk.id, "reason" to "AUTO_HIDDEN"))
            )
            outboxEventPersistable.save(outboxEvent)
        }
        
        return expiredTalks.size
    }

    @Transactional
    override fun updateAuthorNickname(memberId: UUID, newNickname: String) {
        talkPersistable.updateNicknameByMemberId(memberId, newNickname)
    }

    override fun getTalksByBook(bookId: UUID, memberId: UUID?, pageable: Pageable): Page<TalkDetail> {
        return talkLoadable.findByBookId(bookId, pageable).map { talk ->
            toTalkDetail(talk, memberId)
        }
    }

    override fun getRecommendedTalks(size: Int, memberId: UUID?): List<TalkDetail> {
        return talkLoadable.findRecommended(size).map { talk ->
            toTalkDetail(talk, memberId)
        }
    }

    override fun getVisibleTalksByMember(memberId: UUID, pageable: Pageable): Page<TalkDetail> {
        return talkLoadable.findByMemberIdAndIsHidden(memberId, false, pageable).map { talk ->
            toTalkDetail(talk, memberId)
        }
    }

    override fun getHiddenTalksByMember(memberId: UUID, requesterId: UUID, pageable: Pageable): Page<TalkDetail> {
        talkOwnershipService.authorizeHiddenAccess(memberId, requesterId)
        return talkLoadable.findByMemberIdAndIsHidden(memberId, true, pageable).map { talk ->
            toTalkDetail(talk, memberId)
        }
    }

    private fun getTalkOrThrow(talkId: UUID): Talk {
        return talkLoadable.findById(talkId) ?: throw IllegalArgumentException("Talk not found: $talkId")
    }

    private fun toTalkDetail(talk: Talk, memberId: UUID?): TalkDetail {
        val reactedTypes = memberId?.let { 
            reactionLoadable.findMemberReactedTypes(talk.id!!, it)
        } ?: emptySet()

        return with(talk) {
            TalkDetail(
                id = id!!,
                bookId = bookId,
                memberId = talk.memberId,
                nickname = nickname,
                content = content,
                likeCount = likeCount,
                supportCount = supportCount,
                didILike = reactedTypes.contains(ReactionType.LIKE),
                didISupport = reactedTypes.contains(ReactionType.SUPPORT),
                createdAt = createdAt!!,
                isModified = isModified()
            )
        }
    }
}
