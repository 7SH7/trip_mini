package com.study.notification.application.service

import com.study.common.exception.EntityNotFoundException
import com.study.notification.application.dto.NotificationResponse
import com.study.notification.application.dto.UnreadCountResponse
import com.study.notification.domain.entity.Notification
import com.study.notification.domain.entity.NotificationType
import com.study.notification.domain.repository.NotificationRepository
import com.study.notification.infrastructure.sse.SseEmitterRegistry
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val sseEmitterRegistry: SseEmitterRegistry
) {
    @Transactional
    fun createAndSend(userId: Long, title: String, content: String, type: NotificationType, referenceId: String? = null) {
        val notification = notificationRepository.save(
            Notification(userId = userId, title = title, content = content, type = type, referenceId = referenceId)
        )
        sseEmitterRegistry.send(userId, "notification", NotificationResponse.from(notification))
    }

    fun getNotifications(userId: Long, page: Int, size: Int): Page<NotificationResponse> =
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
            .map { NotificationResponse.from(it) }

    fun getUnreadCount(userId: Long): UnreadCountResponse =
        UnreadCountResponse(notificationRepository.countByUserIdAndIsReadFalse(userId))

    @Transactional
    fun markAsRead(userId: Long, id: Long) {
        val notification = notificationRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Notification", id) }
        notification.markRead()
    }

    @Transactional
    fun markAllAsRead(userId: Long) {
        notificationRepository.markAllAsRead(userId)
    }
}
