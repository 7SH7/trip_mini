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
            when (event) {
                is TripCreatedEvent -> {
                    notificationService.createAndSend(
                        userId = event.userId,
                        title = "여행 생성 완료",
                        content = "\"${event.title}\" 여행이 생성되었습니다. 숙소를 검색해보세요!",
                        type = NotificationType.SYSTEM,
                        referenceId = event.tripId.toString()
                    )
                }
                is TripJoinRequestedEvent -> {
                    notificationService.createAndSend(
                        userId = event.ownerUserId,
                        title = "여행 참여 요청",
                        content = "User #${event.requestUserId}님이 \"${event.tripTitle}\" 여행에 참여를 요청했습니다.",
                        type = NotificationType.SYSTEM,
                        referenceId = event.tripId.toString()
                    )
                }
                is TripJoinApprovedEvent -> {
                    notificationService.createAndSend(
                        userId = event.requestUserId,
                        title = "참여 승인",
                        content = "\"${event.tripTitle}\" 여행 참여가 승인되었습니다!",
                        type = NotificationType.SYSTEM,
                        referenceId = event.tripId.toString()
                    )
                }
                is TripJoinRejectedEvent -> {
                    notificationService.createAndSend(
                        userId = event.requestUserId,
                        title = "참여 거절",
                        content = "\"${event.tripTitle}\" 여행 참여가 거절되었습니다.",
                        type = NotificationType.SYSTEM,
                        referenceId = event.tripId.toString()
                    )
                }
            }
        } catch (e: Exception) {
            log.warn("Failed to process trip event: {}", e.message)
        }
    }
}
