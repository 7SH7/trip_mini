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
            is PaymentFailedEvent -> {
                notificationService.createAndSend(
                    userId = event.userId,
                    title = "결제 실패",
                    content = "결제가 실패하여 예약이 자동 취소되었습니다. 사유: ${event.reason}",
                    type = NotificationType.PAYMENT_FAILED,
                    referenceId = event.bookingId.toString()
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

    @KafkaListener(topics = ["chat-events"], groupId = "notification-service")
    fun handleChatEvent(message: String) {
        try {
            val event = objectMapper.readValue(message, DomainEvent::class.java)
            if (event is ChatMessageEvent) {
                log.info("Chat message from user {} in room {}", event.senderId, event.chatRoomId)
                // GPS 기반 채팅방은 고정 멤버가 없으므로 개별 알림 대신 로깅만 수행
            }
        } catch (e: Exception) {
            log.warn("Failed to process chat event: {}", e.message)
        }
    }

    @KafkaListener(topics = ["user-events"], groupId = "notification-service")
    fun handleUserEvent(message: String) {
        try {
            val event = objectMapper.readValue(message, DomainEvent::class.java)
            if (event is UserCreatedEvent) {
                log.info("New user created: {}", event.userId)
                notificationService.createAndSend(
                    userId = event.userId,
                    title = "환영합니다!",
                    content = "Trip 서비스에 가입해주셔서 감사합니다. 여행을 계획해보세요!",
                    type = NotificationType.SYSTEM
                )
            }
        } catch (e: Exception) {
            log.warn("Failed to process user event: {}", e.message)
        }
    }

    @KafkaListener(topics = ["trip-events"], groupId = "notification-service")
    fun handleTripEvent(message: String) {
        try {
            val event = objectMapper.readValue(message, DomainEvent::class.java)
            if (event is TripCreatedEvent) {
                log.info("New trip created: {} by user {}", event.tripId, event.userId)
                notificationService.createAndSend(
                    userId = event.userId,
                    title = "여행 생성 완료",
                    content = "\"${event.title}\" 여행이 생성되었습니다. 숙소를 검색해보세요!",
                    type = NotificationType.SYSTEM,
                    referenceId = event.tripId.toString()
                )
            }
        } catch (e: Exception) {
            log.warn("Failed to process trip event: {}", e.message)
        }
    }
}
