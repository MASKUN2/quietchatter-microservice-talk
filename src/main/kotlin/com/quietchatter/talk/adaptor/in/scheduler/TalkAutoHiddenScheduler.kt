package com.quietchatter.talk.adaptor.`in`.scheduler

import com.quietchatter.talk.application.`in`.TalkCommandable
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TalkAutoHiddenScheduler(
    private val talkCommandable: TalkCommandable
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 0 * * *")
    fun hideExpiredTalks() {
        log.info("Starting auto hidden process for talks...")
        val updatedCount = talkCommandable.hideExpiredTalks()
        log.info("Auto hidden process finished. Updated {} talks.", updatedCount)
    }
}
