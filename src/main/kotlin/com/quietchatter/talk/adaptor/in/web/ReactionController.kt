package com.quietchatter.talk.adaptor.`in`.web

import com.quietchatter.talk.application.`in`.AddReactionCommand
import com.quietchatter.talk.application.`in`.ReactionModifiable
import com.quietchatter.talk.application.`in`.RemoveReactionCommand
import com.quietchatter.talk.domain.ReactionType
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/reactions")
class ReactionController(
    private val reactionModifiable: ReactionModifiable
) {

    @PostMapping("/talks/{talkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
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

    @DeleteMapping("/talks/{talkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
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
