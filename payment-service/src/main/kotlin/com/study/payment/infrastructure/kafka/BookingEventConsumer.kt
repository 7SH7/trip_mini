package com.study.payment.infrastructure.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.study.common.event.BookingCancelledEvent
import com.study.common.event.DomainEvent
import com.study.common.event.PaymentRefundedEvent
import com.study.common.outbox.OutboxEventPublisher
import com.study.payment.domain.repository.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class BookingEventConsumer(
    private val paymentRepository: PaymentRepository,
    private val outboxEventPublisher: OutboxEventPublisher,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["booking-events"], groupId = "payment-service")
    @Transactional
    fun handleBookingEvent(message: String) {
        val event = objectMapper.readValue(message, DomainEvent::class.java)
        if (event is BookingCancelledEvent) {
            log.info("Booking {} cancelled, processing refund", event.bookingId)
            paymentRepository.findByBookingId(event.bookingId).ifPresent { payment ->
                payment.refund()
                paymentRepository.save(payment)
                outboxEventPublisher.publish("payment-events",
                    PaymentRefundedEvent(payment.id, payment.bookingId, payment.userId, payment.amount))
            }
        }
    }
}
