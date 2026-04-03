package com.study.notification.application.dto

import com.study.notification.domain.entity.Notification
import com.study.notification.domain.entity.NotificationType
import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val title: String,
    val content: String,
    val type: NotificationType,
    val isRead: Boolean,
    val referenceId: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(notification: Notification) = NotificationResponse(
            id = notification.id,
            title = notification.title,
            content = notification.content,
            type = notification.type,
            isRead = notification.isRead,
            referenceId = notification.referenceId,
            createdAt = notification.createdAt
        )
    }
}

data class UnreadCountResponse(
    val count: Long
)
