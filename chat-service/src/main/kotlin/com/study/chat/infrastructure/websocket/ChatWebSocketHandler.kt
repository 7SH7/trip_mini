package com.study.chat.infrastructure.websocket

import com.study.chat.application.dto.ChatMessageRequest
import com.study.chat.application.dto.ChatMessageResponse
import com.study.chat.application.service.ChatService
import com.study.chat.domain.entity.MessageType
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class ChatWebSocketHandler(
    private val chatService: ChatService,
    private val messagingTemplate: SimpMessagingTemplate
) {
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    fun handleMessage(
        @DestinationVariable roomId: Long,
        @Header("X-User-Id", defaultValue = "0") userId: String,
        request: ChatMessageRequest
    ): ChatMessageResponse {
        return chatService.saveMessage(roomId, userId.toLong(), request.content, request.type)
    }
}
