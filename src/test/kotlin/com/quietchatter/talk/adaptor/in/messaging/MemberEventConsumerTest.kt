package com.quietchatter.talk.adaptor.`in`.messaging

import com.quietchatter.talk.application.`in`.TalkCommandable
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.messaging.support.MessageBuilder
import java.util.*

class MemberEventConsumerTest {

    private val talkCommandable: TalkCommandable = mock()
    private val memberEventConsumer = MemberEventConsumer(talkCommandable)

    @Test
    fun `should call hideAllByMember when MemberDeactivatedEvent is received`() {
        // Given
        val memberId = UUID.randomUUID()
        val eventDto = MemberEventDto(
            evtType = "MemberDeactivatedEvent",
            memberId = memberId.toString()
        )
        val message = MessageBuilder.withPayload(eventDto).build()

        // When
        memberEventConsumer.memberEvents().accept(message)

        // Then
        verify(talkCommandable).hideAllByMember(memberId)
    }

    @Test
    fun `should ignore other event types`() {
        // Given
        val memberId = UUID.randomUUID()
        val eventDto = MemberEventDto(
            evtType = "OtherEvent",
            memberId = memberId.toString()
        )
        val message = MessageBuilder.withPayload(eventDto).build()

        // When
        memberEventConsumer.memberEvents().accept(message)

        // Then
        verifyNoInteractions(talkCommandable)
    }
}
