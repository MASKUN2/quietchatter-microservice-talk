package com.quietchatter.talk.adaptor.`in`.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.quietchatter.talk.application.`in`.TalkCommandable
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import java.util.function.Consumer

@Configuration
class MemberEventConsumer(
    private val talkCommandable: TalkCommandable,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun memberEvents(): Consumer<String> {
        return Consumer { payload ->
            log.info("Received raw member event message: {}", payload)

            runCatching {
                val eventDto: MemberEventDto = objectMapper.readValue(payload)
                val eventType = eventDto.evtType
                log.info("Processing event type: {}", eventType)

                when (eventType) {
                    "MemberDeactivatedEvent" -> {
                        eventDto.memberId?.let { 
                            val memberId = UUID.fromString(it)
                            log.info("Processing MemberDeactivatedEvent for memberId: {}", memberId)
                            talkCommandable.hideAllByMember(memberId)
                        } ?: log.warn("MemberDeactivatedEvent received but memberId is null")
                    }
                    "MemberProfileUpdatedEvent" -> {
                        val memberIdStr = eventDto.memberId
                        val nickname = eventDto.nickname
                        if (memberIdStr != null && nickname != null) {
                            val memberId = UUID.fromString(memberIdStr)
                            log.info("Processing MemberProfileUpdatedEvent for memberId: {}, newNickname: {}", memberId, nickname)
                            talkCommandable.updateAuthorNickname(memberId, nickname)
                        } else {
                            log.warn("MemberProfileUpdatedEvent received but memberId or nickname is null")
                        }
                    }
                    else -> log.info("Ignored unknown event type: {}", eventType)
                }
            }.onFailure { e ->
                log.error("Failed to process member event payload: $payload", e)
            }
        }
    }
}
