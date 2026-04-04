package com.quietchatter.talk.adaptor.`in`.web

import com.quietchatter.talk.application.`in`.AddReactionCommand
import com.quietchatter.talk.application.`in`.ReactionModifiable
import com.quietchatter.talk.application.`in`.RemoveReactionCommand
import com.quietchatter.talk.domain.ReactionType
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/v1/talks/{talkId}/reactions")
class ReactionController(
    private val reactionModifiable: ReactionModifiable
) {

    @PostMapping
    fun addReaction(
        @RequestHeader("X-Member-Id") memberId: UUID,
        @PathVariable talkId: UUID,
        @RequestBody request: ReactionRequest
    ) {
        reactionModifiable.addReaction(
            AddReactionCommand(
                talkId = talkId,
                memberId = memberId,
                type = request.type
            )
        )
    }

    @DeleteMapping
    fun removeReaction(
        @RequestHeader("X-Member-Id") memberId: UUID,
        @PathVariable talkId: UUID,
        @RequestBody request: ReactionRequest
    ) {
        reactionModifiable.removeReaction(
            RemoveReactionCommand(
                talkId = talkId,
                memberId = memberId,
                type = request.type
            )
        )
    }
}

data class ReactionRequest(
    val type: ReactionType
)
