package com.study.chat.interfaces.controller

import com.study.chat.application.dto.ChatMessageResponse
import com.study.chat.application.dto.ChatRoomResponse
import com.study.chat.application.dto.NearbyRoomRequest
import com.study.chat.application.service.ChatService
import com.study.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService
) {
    @PostMapping("/rooms/nearby")
    fun findNearbyRoom(
        @Valid @RequestBody request: NearbyRoomRequest
    ): ApiResponse<ChatRoomResponse> = ApiResponse.ok(chatService.findOrCreateNearbyRoom(request))

    @GetMapping("/rooms/{roomId}")
    fun getRoom(@PathVariable roomId: Long): ApiResponse<ChatRoomResponse> =
        ApiResponse.ok(chatService.getRoom(roomId))

    @GetMapping("/rooms/{roomId}/messages")
    fun getMessages(
        @PathVariable roomId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ApiResponse<Page<ChatMessageResponse>> =
        ApiResponse.ok(chatService.getMessages(roomId, page, size))

    @GetMapping("/rooms/{roomId}/online")
    fun getOnlineUsers(@PathVariable roomId: Long): ApiResponse<Set<Long>> =
        ApiResponse.ok(chatService.getOnlineUsers(roomId))

    @PostMapping("/rooms/{roomId}/join")
    fun joinRoom(
        @PathVariable roomId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<Unit?> {
        chatService.joinRoom(roomId, userId)
        return ApiResponse(200, "Joined", null)
    }

    @PostMapping("/rooms/{roomId}/leave")
    fun leaveRoom(
        @PathVariable roomId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<Unit?> {
        chatService.leaveRoom(roomId, userId)
        return ApiResponse(200, "Left", null)
    }
}
