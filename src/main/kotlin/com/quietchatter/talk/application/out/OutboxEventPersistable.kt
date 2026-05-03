package com.quietchatter.talk.application.out

import com.quietchatter.talk.adaptor.out.outbox.OutboxEvent
import java.time.LocalDateTime
import java.util.UUID

interface OutboxEventPersistable {
    fun save(event: OutboxEvent): OutboxEvent
    fun findUnprocessed(limit: Int): List<OutboxEvent>
    fun deleteProcessedBefore(cutoff: LocalDateTime): Long
}
