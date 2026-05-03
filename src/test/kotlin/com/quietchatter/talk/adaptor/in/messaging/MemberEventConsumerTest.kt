package com.quietchatter.talk.adaptor.`in`.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.quietchatter.talk.application.`in`.TalkCommandable
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.messaging.support.MessageBuilder
import java.util.*

class MemberEventConsumerTest {

    private val talkCommandable: TalkCommandable = mock()
    private val objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule()
    private val memberEventConsumer = MemberEventConsumer(talkCommandable, objectMapper)

    @Test
    fun `should call hideAllByMember when MemberDeactivatedEvent is received`() {
        // Given
        val memberId = UUID.randomUUID()
        val json = """
            {
                "evt_type": "MemberDeactivatedEvent",
                "memberId": "$memberId"
            }
        """.trimIndent()
        val message = MessageBuilder.withPayload(json).build()

        // When
        memberEventConsumer.memberEvents().accept(message)

        // Then
        verify(talkCommandable).hideAllByMember(memberId)
    }

    @Test
    fun `should ignore other event types`() {
        // Given
        val json = """
            {
                "evt_type": "OtherEvent",
                "memberId": "${UUID.randomUUID()}"
            }
        """.trimIndent()
        val message = MessageBuilder.withPayload(json).build()

        // When
        memberEventConsumer.memberEvents().accept(message)

        // Then
        verifyNoInteractions(talkCommandable)
    }
}
