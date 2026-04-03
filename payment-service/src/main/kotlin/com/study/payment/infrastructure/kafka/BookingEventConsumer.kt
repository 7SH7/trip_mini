package com.study.payment.infrastructure.kafka

import com.study.common.event.BookingCancelledEvent
import com.study.common.event.DomainEvent
import com.study.common.event.PaymentRefundedEvent
import com.study.payment.domain.repository.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class BookingEventConsumer(
    private val paymentRepository: PaymentRepository,
    private val kafkaTemplate: KafkaTemplate<String, DomainEvent>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["booking-events"], groupId = "payment-service")
    @Transactional
    fun handleBookingEvent(event: DomainEvent) {
        if (event is BookingCancelledEvent) {
            log.info("Booking {} cancelled, processing refund", event.bookingId)
            paymentRepository.findByBookingId(event.bookingId).ifPresent { payment ->
                payment.refund()
                paymentRepository.save(payment)
                kafkaTemplate.send("payment-events", PaymentRefundedEvent(payment.id, payment.bookingId, payment.userId, payment.amount))
            }
        }
    }
}
