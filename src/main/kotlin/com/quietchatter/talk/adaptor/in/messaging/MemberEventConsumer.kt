package com.quietchatter.talk.adaptor.`in`.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.quietchatter.talk.application.`in`.TalkCommandable
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.util.*
import java.util.function.Consumer

@Configuration
class MemberEventConsumer(
    private val talkCommandable: TalkCommandable,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun memberEvents(): Consumer<Message<String>> {
        return Consumer { message ->
            val eventType = message.headers["eventType"] as? String
            log.debug("Received member event: {}", eventType)

            if (eventType == "MemberDeactivatedEvent") {
                try {
                    val payload = objectMapper.readTree(message.payload)
                    val memberIdStr = payload.get("memberId")?.asText()
                    
                    if (memberIdStr != null) {
                        val memberId = UUID.fromString(memberIdStr)
                        log.info("Processing MemberDeactivatedEvent for memberId: {}", memberId)
                        talkCommandable.hideAllByMember(memberId)
                    }
                } catch (e: Exception) {
                    log.error("Failed to process MemberDeactivatedEvent", e)
                    throw e
                }
            }
        }
    }
}
