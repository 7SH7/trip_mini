package com.study.notification.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.notification.application.dto.NotificationResponse
import com.study.notification.application.dto.UnreadCountResponse
import com.study.notification.application.service.NotificationService
import com.study.notification.infrastructure.sse.SseEmitterRegistry
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val sseEmitterRegistry: SseEmitterRegistry
) {
    @GetMapping("/subscribe")
    fun subscribe(@RequestHeader("X-User-Id") userId: Long): SseEmitter =
        sseEmitterRegistry.register(userId)

    @GetMapping
    fun getNotifications(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<Page<NotificationResponse>> =
        ApiResponse.ok(notificationService.getNotifications(userId, page, size))

    @GetMapping("/unread-count")
    fun getUnreadCount(@RequestHeader("X-User-Id") userId: Long): ApiResponse<UnreadCountResponse> =
        ApiResponse.ok(notificationService.getUnreadCount(userId))

    @PatchMapping("/{id}/read")
    fun markAsRead(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: Long
    ): ApiResponse<Unit?> {
        notificationService.markAsRead(userId, id)
        return ApiResponse(200, "OK", null)
    }

    @PatchMapping("/read-all")
    fun markAllAsRead(@RequestHeader("X-User-Id") userId: Long): ApiResponse<Unit?> {
        notificationService.markAllAsRead(userId)
        return ApiResponse(200, "OK", null)
    }
}
