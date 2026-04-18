package com.quietchatter.talk.adaptor.`in`.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.quietchatter.member.adaptor.out.messaging.avro.MemberEventAvro
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
        val avroEvent = MemberEventAvro.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setAggregateId(memberId.toString())
            .setType("MemberDeactivatedEvent")
            .setPayload(payload)
            .setOccurredAt("now")
            .build()
        val message = MessageBuilder.withPayload(avroEvent).build()

        // When
        memberEventConsumer.memberEvents().accept(message)

        // Then
        verify(talkCommandable).hideAllByMember(memberId)
    }

    @Test
    fun `should ignore other event types`() {
        // Given
        val memberId = UUID.randomUUID()
        val payload = """{"memberId": "$memberId"}"""
        val avroEvent = MemberEventAvro.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setAggregateId(memberId.toString())
            .setType("OtherEvent")
            .setPayload(payload)
            .setOccurredAt("now")
            .build()
        val message = MessageBuilder.withPayload(avroEvent).build()

        // When
        memberEventConsumer.memberEvents().accept(message)

        // Then
        verifyNoInteractions(talkCommandable)
    }
}
