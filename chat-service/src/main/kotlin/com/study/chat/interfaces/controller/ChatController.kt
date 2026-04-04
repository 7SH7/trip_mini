package com.study.chat.interfaces.controller

import com.study.chat.application.dto.ChatMessageResponse
import com.study.chat.application.dto.ChatRoomResponse
import com.study.chat.application.dto.NearbyRoomRequest
import com.study.chat.application.service.ChatService
import com.study.common.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*

@Tag(name = "채팅", description = "GPS 기반 채팅방, 메시지 조회")
@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService
) {
    @Operation(summary = "주변 채팅방 검색 또는 생성")
    @PostMapping("/rooms/nearby")
    fun findNearbyRoom(
        @Valid @RequestBody request: NearbyRoomRequest
    ): ApiResponse<ChatRoomResponse> = ApiResponse.ok(chatService.findOrCreateNearbyRoom(request))

    @Operation(summary = "채팅방 상세 조회")
    @GetMapping("/rooms/{roomId}")
    fun getRoom(@PathVariable roomId: Long): ApiResponse<ChatRoomResponse> =
        ApiResponse.ok(chatService.getRoom(roomId))

    @Operation(summary = "채팅 메시지 목록 조회")
    @GetMapping("/rooms/{roomId}/messages")
    fun getMessages(
        @PathVariable roomId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ApiResponse<Page<ChatMessageResponse>> =
        ApiResponse.ok(chatService.getMessages(roomId, page, size))

    @Operation(summary = "채팅방 온라인 사용자 목록 조회")
    @GetMapping("/rooms/{roomId}/online")
    fun getOnlineUsers(@PathVariable roomId: Long): ApiResponse<Set<Long>> =
        ApiResponse.ok(chatService.getOnlineUsers(roomId))

    @Operation(summary = "채팅방 입장")
    @PostMapping("/rooms/{roomId}/join")
    fun joinRoom(
        @PathVariable roomId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<Unit?> {
        chatService.joinRoom(roomId, userId)
        return ApiResponse(200, "Joined", null)
    }

    @Operation(summary = "채팅방 퇴장")
    @PostMapping("/rooms/{roomId}/leave")
    fun leaveRoom(
        @PathVariable roomId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<Unit?> {
        chatService.leaveRoom(roomId, userId)
        return ApiResponse(200, "Left", null)
    }
}
