package com.quietchatter.talk.adaptor.out.persistence

import com.quietchatter.talk.application.out.ReactionLoadable
import com.quietchatter.talk.application.out.ReactionPersistable
import com.quietchatter.talk.domain.Reaction
import com.quietchatter.talk.domain.ReactionType
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ReactionPersistenceAdapter(
    private val reactionJpaRepository: ReactionJpaRepository
) : ReactionPersistable, ReactionLoadable {

    override fun save(reaction: Reaction): Reaction {
        return reactionJpaRepository.save(reaction)
    }

    override fun delete(reaction: Reaction) {
        reactionJpaRepository.delete(reaction)
    }

    override fun findByTalkIdAndMemberIdAndType(talkId: UUID, memberId: UUID, type: ReactionType): Reaction? {
        return reactionJpaRepository.findByTalkIdAndMemberIdAndType(talkId, memberId, type)
    }

    override fun existsByTalkIdAndMemberId(talkId: UUID, memberId: UUID): Boolean {
        return reactionJpaRepository.existsByTalkIdAndMemberId(talkId, memberId)
    }

    override fun findMemberReactedTalkIds(talkIds: List<UUID>, memberId: UUID): Set<UUID> {
        if (talkIds.isEmpty()) return emptySet()
        return reactionJpaRepository.findReactedTalkIds(talkIds, memberId).toSet()
    }

    override fun findMemberReactedTypes(talkId: UUID, memberId: UUID): Set<ReactionType> {
        return reactionJpaRepository.findTypesByTalkIdAndMemberId(talkId, memberId).toSet()
    }
}
