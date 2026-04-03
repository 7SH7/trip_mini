package com.study.chat.application.dto

import com.study.chat.domain.entity.ChatMessage
import com.study.chat.domain.entity.ChatRoom
import com.study.chat.domain.entity.MessageType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class NearbyRoomRequest(
    @field:NotNull val latitude: Double? = null,
    @field:NotNull val longitude: Double? = null
)

data class ChatRoomResponse(
    val id: Long,
    val name: String,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val onlineCount: Int = 0
) {
    companion object {
        fun from(room: ChatRoom, onlineCount: Int = 0) = ChatRoomResponse(
            id = room.id, name = room.name,
            centerLatitude = room.centerLatitude, centerLongitude = room.centerLongitude,
            onlineCount = onlineCount
        )
    }
}

data class ChatMessageRequest(
    @field:NotBlank val content: String = "",
    val type: MessageType = MessageType.TEXT
)

data class ChatMessageResponse(
    val id: Long,
    val chatRoomId: Long,
    val userId: Long,
    val content: String,
    val type: MessageType,
    val sentAt: LocalDateTime
) {
    companion object {
        fun from(msg: ChatMessage) = ChatMessageResponse(
            id = msg.id, chatRoomId = msg.chatRoomId, userId = msg.userId,
            content = msg.content, type = msg.type, sentAt = msg.sentAt
        )
    }
}
