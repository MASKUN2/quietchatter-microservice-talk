package com.quietchatter.talk.application

import com.quietchatter.talk.application.`in`.*
import com.quietchatter.talk.application.out.ReactionLoadable
import com.quietchatter.talk.application.out.TalkLoadable
import com.quietchatter.talk.application.out.TalkPersistable
import com.quietchatter.talk.domain.ReactionType
import com.quietchatter.talk.domain.Talk
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
    private val outboxEventRepository: com.quietchatter.talk.adaptor.out.outbox.OutboxEventRepository,
    private val memberClient: com.quietchatter.talk.adaptor.out.external.MemberClient
) : TalkCommandable, TalkQueryable {

    @Transactional
    override fun createTalk(command: CreateTalkCommand): UUID {
        val memberInfo = memberClient.getMemberInfo(command.memberId)
        
        val talk = Talk(
            bookId = command.bookId,
            memberId = command.memberId,
            nickname = memberInfo.nickname,
            content = command.content,
            dateToHidden = command.dateToHidden
        )
        return talkPersistable.save(talk).id!!
    }

    @Transactional
    override fun updateTalk(command: UpdateTalkCommand) {
        val talk = talkLoadable.findById(command.talkId) ?: throw IllegalArgumentException("Talk not found")
        require(talk.memberId == command.memberId) { "Only the author can update the talk" }
        talk.updateContent(command.content)
        talkPersistable.save(talk)
    }

    @Transactional
    override fun deleteTalk(command: DeleteTalkCommand) {
        val talk = talkLoadable.findById(command.talkId) ?: throw IllegalArgumentException("Talk not found")
        require(talk.memberId == command.memberId) { "Only the author can delete the talk" }
        talk.hide()
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
            talk.hide()
            talkPersistable.save(talk)
            
            val outboxEvent = com.quietchatter.talk.adaptor.out.outbox.OutboxEvent(
                aggregateType = "Talk",
                aggregateId = talk.id.toString(),
                type = "TalkHiddenEvent",
                payload = "{\"talkId\": \"${talk.id}\", \"reason\": \"AUTO_HIDDEN\"}"
            )
            outboxEventRepository.save(outboxEvent)
        }
        
        return expiredTalks.size
    }

    @Transactional
    override fun updateAuthorNickname(memberId: UUID, newNickname: String) {
        talkPersistable.updateNicknameByMemberId(memberId, newNickname)
    }

    override fun getTalksByBook(bookId: UUID, memberId: UUID?, pageable: Pageable): Page<TalkDetail> {
        val talks = talkLoadable.findByBookId(bookId, pageable)
        return talks.map { talk ->
            val reactedTypes = memberId?.let { 
                reactionLoadable.findMemberReactedTypes(talk.id!!, it)
            } ?: emptySet()

            TalkDetail(
                id = talk.id!!,
                bookId = talk.bookId,
                memberId = talk.memberId,
                nickname = talk.nickname,
                content = talk.content,
                likeCount = talk.likeCount,
                supportCount = talk.supportCount,
                didILike = reactedTypes.contains(ReactionType.LIKE),
                didISupport = reactedTypes.contains(ReactionType.SUPPORT),
                createdAt = talk.createdAt!!,
                isModified = talk.isModified()
            )
        }
    }

    override fun getRecommendedTalks(size: Int, memberId: UUID?): List<TalkDetail> {
        val talks = talkLoadable.findRecommended(size)
        return talks.map { talk ->
            val reactedTypes = memberId?.let { 
                reactionLoadable.findMemberReactedTypes(talk.id!!, it)
            } ?: emptySet()

            TalkDetail(
                id = talk.id!!,
                bookId = talk.bookId,
                memberId = talk.memberId,
                nickname = talk.nickname,
                content = talk.content,
                likeCount = talk.likeCount,
                supportCount = talk.supportCount,
                didILike = reactedTypes.contains(ReactionType.LIKE),
                didISupport = reactedTypes.contains(ReactionType.SUPPORT),
                createdAt = talk.createdAt!!,
                isModified = talk.isModified()
            )
        }
    }

    override fun getTalksByMember(memberId: UUID, pageable: Pageable): Page<TalkDetail> {
        val talks = talkLoadable.findByMemberId(memberId, pageable)
        return talks.map { talk ->
            val reactedTypes = reactionLoadable.findMemberReactedTypes(talk.id!!, memberId)

            TalkDetail(
                id = talk.id!!,
                bookId = talk.bookId,
                memberId = talk.memberId,
                nickname = talk.nickname,
                content = talk.content,
                likeCount = talk.likeCount,
                supportCount = talk.supportCount,
                didILike = reactedTypes.contains(ReactionType.LIKE),
                didISupport = reactedTypes.contains(ReactionType.SUPPORT),
                createdAt = talk.createdAt!!,
                isModified = talk.isModified()
            )
        }
    }
}
