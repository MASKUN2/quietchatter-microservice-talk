package com.quietchatter.talk.application.`out`

import com.quietchatter.talk.domain.Reaction
import com.quietchatter.talk.domain.ReactionType
import java.util.UUID

interface ReactionLoadable {
    fun findByTalkIdAndMemberIdAndType(talkId: UUID, memberId: UUID, type: ReactionType): Reaction?
    fun existsByTalkIdAndMemberId(talkId: UUID, memberId: UUID): Boolean
    fun findMemberReactedTalkIds(talkIds: List<UUID>, memberId: UUID): Set<UUID>
    fun findMemberReactedTypes(talkId: UUID, memberId: UUID): Set<ReactionType>
}
