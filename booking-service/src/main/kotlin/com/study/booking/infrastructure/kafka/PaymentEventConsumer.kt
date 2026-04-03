package com.study.booking.infrastructure.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.study.booking.domain.repository.BookingRepository
import com.study.common.event.DomainEvent
import com.study.common.event.PaymentCompletedEvent
import com.study.common.event.PaymentFailedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PaymentEventConsumer(
    private val bookingRepository: BookingRepository,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["payment-events"], groupId = "booking-service")
    @Transactional
    fun handlePaymentEvent(message: String) {
        val event = objectMapper.readValue(message, DomainEvent::class.java)
        when (event) {
            is PaymentCompletedEvent -> {
                log.info("Payment completed for booking {}", event.bookingId)
                bookingRepository.findById(event.bookingId).ifPresent { booking ->
                    booking.confirm()
                    bookingRepository.save(booking)
                    log.info("Booking {} confirmed after payment", booking.id)
                }
            }
            is PaymentFailedEvent -> {
                log.warn("Payment failed for booking {}: {}", event.bookingId, event.reason)
                bookingRepository.findById(event.bookingId).ifPresent { booking ->
                    booking.cancel()
                    bookingRepository.save(booking)
                    log.info("Booking {} cancelled due to payment failure (compensating transaction)", booking.id)
                }
            }
        }
    }
}
