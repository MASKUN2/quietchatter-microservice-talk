package com.quietchatter.talk.application.`in`

import com.quietchatter.talk.domain.ReactionType
import java.util.UUID

interface ReactionModifiable {
    fun addReaction(command: AddReactionCommand)
    fun removeReaction(command: RemoveReactionCommand)
}

data class AddReactionCommand(
    val talkId: UUID,
    val memberId: UUID,
    val type: ReactionType
)

data class RemoveReactionCommand(
    val talkId: UUID,
    val memberId: UUID,
    val type: ReactionType
)
