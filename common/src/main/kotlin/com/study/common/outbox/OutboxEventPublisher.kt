package com.study.common.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.study.common.event.DomainEvent
import org.springframework.stereotype.Component

@Component
class OutboxEventPublisher(
    private val outboxRepository: OutboxRepository,
    private val objectMapper: ObjectMapper
) {
    fun publish(topic: String, event: DomainEvent) {
        val payload = objectMapper.writeValueAsString(event)
        outboxRepository.save(
            OutboxEvent(
                topic = topic,
                payload = payload,
                eventType = event::class.qualifiedName ?: event::class.simpleName ?: "Unknown"
            )
        )
    }
}
