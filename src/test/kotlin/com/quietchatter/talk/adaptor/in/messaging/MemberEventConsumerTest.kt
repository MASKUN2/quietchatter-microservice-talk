package com.quietchatter.talk.adaptor.`in`.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.quietchatter.talk.application.`in`.TalkCommandable
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.messaging.support.MessageBuilder
import java.util.*

class MemberEventConsumerTest {

    private val talkCommandable: TalkCommandable = mock()
    private val objectMapper = ObjectMapper()
    private val memberEventConsumer = MemberEventConsumer(talkCommandable, objectMapper)

    @Test
    fun `should call hideAllByMember when MemberDeactivatedEvent is received`() {
        // Given
        val memberId = UUID.randomUUID()
        val payload = """{"memberId": "$memberId"}"""
        val message = MessageBuilder.withPayload(payload)
            .setHeader("eventType", "MemberDeactivatedEvent")
            .build()

        // When
        memberEventConsumer.memberEvents().accept(message)

        // Then
        verify(talkCommandable).hideAllByMember(memberId)
    }

    @Test
    fun `should ignore other event types`() {
        // Given
        val payload = """{"memberId": "${UUID.randomUUID()}"}"""
        val message = MessageBuilder.withPayload(payload)
            .setHeader("eventType", "OtherEvent")
            .build()

        // When
        memberEventConsumer.memberEvents().accept(message)

        // Then
        verifyNoInteractions(talkCommandable)
    }
}
