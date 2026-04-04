package com.quietchatter.talk.adaptor.`in`.web

import com.quietchatter.talk.application.`in`.TalkCommandable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/internal/v1/talks")
class InternalTalkController(
    private val talkCommandable: TalkCommandable
) {

    @DeleteMapping("/by-member/{memberId}")
    fun hideAllByMember(@PathVariable memberId: UUID) {
        talkCommandable.hideAllByMember(memberId)
    }
}
