package com.quietchatter.talk.adaptor.`in`.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.quietchatter.talk.application.`in`.TalkCommandable
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import java.util.*

class MemberEventConsumerTest {

    private val talkCommandable: TalkCommandable = mock()
    private val objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule()
    private val memberEventConsumer = MemberEventConsumer(talkCommandable, objectMapper)

    @Test
    fun `should call hideAllByMember when MemberDeactivatedEvent is received`() {
        val memberId = UUID.randomUUID()
        val json = """
            {
                "specversion": "1.0",
                "id": "event-id",
                "source": "/member",
                "type": "com.quietchatter.member.MemberDeactivatedEvent",
                "time": "2026-05-03T00:00:00",
                "subject": "$memberId",
                "datacontenttype": "application/json",
                "data": { "memberId": "$memberId" }
            }
        """.trimIndent()

        memberEventConsumer.memberEvents().accept(json)

        verify(talkCommandable).hideAllByMember(memberId)
    }

    @Test
    fun `should ignore other event types`() {
        val json = """
            {
                "specversion": "1.0",
                "id": "event-id",
                "source": "/member",
                "type": "com.quietchatter.member.OtherEvent",
                "time": "2026-05-03T00:00:00",
                "subject": "${UUID.randomUUID()}",
                "datacontenttype": "application/json",
                "data": {}
            }
        """.trimIndent()

        memberEventConsumer.memberEvents().accept(json)

        verifyNoInteractions(talkCommandable)
    }

    @Test
    fun `should throw exception on malformed payload so retry and DLQ can trigger`() {
        assertThrows<Exception> {
            memberEventConsumer.memberEvents().accept("not-valid-json")
        }
    }
}
