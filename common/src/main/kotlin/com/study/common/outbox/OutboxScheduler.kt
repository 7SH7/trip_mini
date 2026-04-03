package com.study.common.outbox

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OutboxScheduler(
    private val outboxRepository: OutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1000)
    @Transactional
    fun publishPendingEvents() {
        val events = outboxRepository.findByStatusOrderByCreatedAt(OutboxStatus.PENDING)
        for (event in events) {
            try {
                kafkaTemplate.send(event.topic, event.payload).get()
                event.markPublished()
                outboxRepository.save(event)
                log.debug("Published outbox event {} to {}", event.id, event.topic)
            } catch (e: Exception) {
                event.markFailed()
                outboxRepository.save(event)
                log.error("Failed to publish outbox event {} to {}", event.id, event.topic, e)
            }
        }
    }
}
