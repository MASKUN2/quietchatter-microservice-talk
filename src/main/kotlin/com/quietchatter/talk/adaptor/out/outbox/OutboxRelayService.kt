package com.quietchatter.talk.adaptor.out.outbox

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.quietchatter.talk.adaptor.out.messaging.TalkIntegrationEvent
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutboxRelayService(
    private val outboxEventRepository: OutboxEventRepository,
    private val streamBridge: StreamBridge,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(OutboxRelayService::class.java)
    private val mapTypeReference = object : TypeReference<Map<String, Any?>>() {}

    @Scheduled(fixedDelayString = "\${outbox.relay.fixed-delay:1000}")
    @Transactional
    fun relayEvents() {
        val events = outboxEventRepository.findByProcessedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, 100))
        for (event in events) {
            val payloadMap = try {
                objectMapper.readValue(event.payload, mapTypeReference)
            } catch (e: Exception) {
                log.error("Failed to parse event payload for outbox event: \${event.id}", e)
                continue
            }

            val integrationEvent = TalkIntegrationEvent(
                evtId = event.id.toString(),
                evtAggId = event.aggregateId,
                evtType = event.type,
                evtTime = event.createdAt.toString(),
                payload = payloadMap
            )

            val message = MessageBuilder.withPayload(integrationEvent)
                .setHeader(KafkaHeaders.KEY, event.aggregateId.toByteArray())
                .build()

            val success = streamBridge.send("talkEvents-out-0", message)
            if (success) {
                event.markProcessed()
                log.debug("Successfully relayed outbox event: \${event.id}")
            } else {
                log.error("Failed to relay outbox event: \${event.id}")
            }
        }
    }
}