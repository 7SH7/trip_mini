package com.study.notification.infrastructure.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.study.common.event.*
import com.study.notification.application.service.NotificationService
import com.study.notification.domain.entity.NotificationType
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class EventConsumers(
    private val notificationService: NotificationService,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["booking-events"], groupId = "notification-service")
    fun handleBookingEvent(message: String) {
        val event = objectMapper.readValue(message, DomainEvent::class.java)
        when (event) {
            is BookingConfirmedEvent -> {
                log.info("Booking confirmed notification for user {}", event.userId)
                notificationService.createAndSend(
                    userId = event.userId,
                    title = "예약 확정",
                    content = "예약 #${event.bookingId}이 확정되었습니다.",
                    type = NotificationType.BOOKING_CONFIRMED,
                    referenceId = event.bookingId.toString()
                )
            }
            is BookingCancelledEvent -> {
                notificationService.createAndSend(
                    userId = event.userId,
                    title = "예약 취소",
                    content = "예약 #${event.bookingId}이 취소되었습니다.",
                    type = NotificationType.BOOKING_CANCELLED,
                    referenceId = event.bookingId.toString()
                )
            }
        }
    }

    @KafkaListener(topics = ["payment-events"], groupId = "notification-service")
    fun handlePaymentEvent(message: String) {
        val event = objectMapper.readValue(message, DomainEvent::class.java)
        when (event) {
            is PaymentCompletedEvent -> {
                notificationService.createAndSend(
                    userId = event.userId,
                    title = "결제 완료",
                    content = "결제 #${event.paymentId}이 완료되었습니다. (${event.amount}원)",
                    type = NotificationType.PAYMENT_COMPLETED,
                    referenceId = event.paymentId.toString()
                )
            }
            is PaymentRefundedEvent -> {
                notificationService.createAndSend(
                    userId = event.userId,
                    title = "환불 완료",
                    content = "결제 #${event.paymentId}이 환불되었습니다. (${event.amount}원)",
                    type = NotificationType.PAYMENT_REFUNDED,
                    referenceId = event.paymentId.toString()
                )
            }
        }
    }
}
