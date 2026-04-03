package com.study.payment.infrastructure.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.study.common.event.BookingCancelledEvent
import com.study.common.event.DomainEvent
import com.study.common.event.PaymentRefundedEvent
import com.study.common.outbox.OutboxEventPublisher
import com.study.payment.domain.entity.PaymentStatus
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
                if (payment.status == PaymentStatus.COMPLETED) {
                    try {
                        payment.refund()
                        paymentRepository.save(payment)
                        outboxEventPublisher.publish("payment-events",
                            PaymentRefundedEvent(payment.id, payment.bookingId, payment.userId, payment.amount))
                        log.info("Payment {} refunded for booking {}", payment.id, event.bookingId)
                    } catch (e: Exception) {
                        if (payment.incrementRetry()) {
                            paymentRepository.save(payment)
                            log.warn("Refund retry {}/{} for payment {}", payment.refundRetryCount, 3, payment.id)
                            throw e // re-throw to trigger Kafka retry
                        } else {
                            payment.status = PaymentStatus.FAILED
                            paymentRepository.save(payment)
                            log.error("Refund failed after {} retries for payment {}", payment.refundRetryCount, payment.id)
                        }
                    }
                }
            }
        }
    }
}
