package com.quietchatter.talk.adaptor.out.outbox

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.quietchatter.talk.adaptor.out.messaging.TalkIntegrationEvent
import com.quietchatter.talk.application.out.OutboxEventPersistable
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Component
class OutboxPersistenceAdapter(
    private val outboxEventRepository: OutboxEventRepository
) : OutboxEventPersistable {
    override fun save(event: OutboxEvent): OutboxEvent = outboxEventRepository.save(event)

    override fun findUnprocessed(limit: Int): List<OutboxEvent> {
        return outboxEventRepository.findByProcessedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, limit))
    }
}

@Service
class OutboxRelayService(
    private val outboxEventPersistable: OutboxEventPersistable,
    private val streamBridge: StreamBridge,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(OutboxRelayService::class.java)
    private val mapTypeReference = object : TypeReference<Map<String, Any?>>() {}

    @Scheduled(fixedDelayString = "\${outbox.relay.fixed-delay:1000}")
    @Transactional
    fun relayEvents() {
        val events = outboxEventPersistable.findUnprocessed(100)
        events.forEach { event ->
            runCatching {
                val payloadMap = objectMapper.readValue(event.payload, mapTypeReference)
                val integrationEvent = TalkIntegrationEvent(
                    id = event.id.toString(),
                    source = "/talk",
                    type = "com.quietchatter.talk.${event.type}",
                    time = event.createdAt.toString(),
                    subject = event.aggregateId,
                    data = payloadMap
                )

                val message = MessageBuilder.withPayload(integrationEvent)
                    .setHeader(KafkaHeaders.KEY, event.aggregateId.toByteArray())
                    .build()

                if (streamBridge.send("talkEvents-out-0", message)) {
                    event.markProcessed()
                    outboxEventPersistable.save(event)
                    log.debug("Successfully relayed outbox event: \${event.id}")
                } else {
                    log.error("Failed to relay outbox event: \${event.id}")
                }
            }.onFailure { e ->
                log.error("Error processing outbox event: \${event.id}", e)
            }
        }
    }
}
