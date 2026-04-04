package com.quietchatter.talk.adaptor.`in`.web

import com.quietchatter.talk.application.out.ReactionLoadable
import com.quietchatter.talk.domain.ReactionType
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/talks/internal")
class InternalTalkController(
    private val reactionLoadable: ReactionLoadable
) {

    @GetMapping("/reactions/stats")
    fun getReactionStats(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) start: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) end: LocalDateTime
    ): Map<ReactionType, Long> {
        return reactionLoadable.countByCreatedAtBetweenGroupByType(start, end)
    }
}
