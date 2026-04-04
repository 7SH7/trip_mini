package com.study.notification.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.notification.application.dto.NotificationResponse
import com.study.notification.application.dto.UnreadCountResponse
import com.study.notification.application.service.NotificationService
import com.study.notification.infrastructure.sse.SseEmitterRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Tag(name = "알림", description = "SSE 알림 구독, 읽음 처리")
@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val sseEmitterRegistry: SseEmitterRegistry
) {
    @Operation(summary = "SSE 알림 구독")
    @GetMapping("/subscribe")
    fun subscribe(@RequestHeader("X-User-Id") userId: Long): SseEmitter =
        sseEmitterRegistry.register(userId)

    @Operation(summary = "알림 목록 조회")
    @GetMapping
    fun getNotifications(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<Page<NotificationResponse>> =
        ApiResponse.ok(notificationService.getNotifications(userId, page, size))

    @Operation(summary = "읽지 않은 알림 수 조회")
    @GetMapping("/unread-count")
    fun getUnreadCount(@RequestHeader("X-User-Id") userId: Long): ApiResponse<UnreadCountResponse> =
        ApiResponse.ok(notificationService.getUnreadCount(userId))

    @Operation(summary = "알림 읽음 처리")
    @PatchMapping("/{id}/read")
    fun markAsRead(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: Long
    ): ApiResponse<Unit?> {
        notificationService.markAsRead(userId, id)
        return ApiResponse(200, "OK", null)
    }

    @Operation(summary = "모든 알림 읽음 처리")
    @PatchMapping("/read-all")
    fun markAllAsRead(@RequestHeader("X-User-Id") userId: Long): ApiResponse<Unit?> {
        notificationService.markAllAsRead(userId)
        return ApiResponse(200, "OK", null)
    }
}
