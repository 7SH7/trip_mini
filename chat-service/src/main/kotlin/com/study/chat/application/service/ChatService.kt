package com.study.chat.application.service

import com.study.chat.application.dto.ChatMessageResponse
import com.study.chat.application.dto.ChatRoomResponse
import com.study.chat.application.dto.NearbyRoomRequest
import com.study.chat.domain.entity.ChatMessage
import com.study.chat.domain.entity.ChatRoom
import com.study.chat.domain.entity.MessageType
import com.study.chat.domain.repository.ChatMessageRepository
import com.study.chat.domain.repository.ChatRoomRepository
import com.study.chat.infrastructure.redis.OnlineUserTracker
import com.study.common.exception.EntityNotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ChatService(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val onlineUserTracker: OnlineUserTracker,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${chat.room-radius-km}") private val defaultRadius: Double
) {
    @Transactional
    fun findOrCreateNearbyRoom(request: NearbyRoomRequest): ChatRoomResponse {
        val lat = requireNotNull(request.latitude) { "Latitude is required" }
        val lng = requireNotNull(request.longitude) { "Longitude is required" }

        val nearbyRooms = chatRoomRepository.findNearbyRooms(lat, lng)
        val room = nearbyRooms.firstOrNull() ?: chatRoomRepository.save(
            ChatRoom(
                name = "Chat Room (${String.format("%.2f", lat)}, ${String.format("%.2f", lng)})",
                centerLatitude = lat,
                centerLongitude = lng,
                radiusKm = defaultRadius
            )
        )
        val onlineCount = onlineUserTracker.getOnlineUsers(room.id).size
        return ChatRoomResponse.from(room, onlineCount)
    }

    fun getRoom(roomId: Long): ChatRoomResponse {
        val room = chatRoomRepository.findById(roomId)
            .orElseThrow { EntityNotFoundException("ChatRoom", roomId) }
        val onlineCount = onlineUserTracker.getOnlineUsers(roomId).size
        return ChatRoomResponse.from(room, onlineCount)
    }

    fun getMessages(roomId: Long, page: Int, size: Int): Page<ChatMessageResponse> =
        chatMessageRepository.findByChatRoomIdOrderBySentAtDesc(roomId, PageRequest.of(page, size))
            .map { ChatMessageResponse.from(it) }

    @Transactional
    fun saveMessage(roomId: Long, userId: Long, content: String, type: MessageType): ChatMessageResponse {
        val message = chatMessageRepository.save(
            ChatMessage(chatRoomId = roomId, userId = userId, content = content, type = type)
        )
        // Publish to Kafka for notification-service
        kafkaTemplate.send("chat-events", """{"chatRoomId":$roomId,"userId":$userId,"content":"$content"}""")
        return ChatMessageResponse.from(message)
    }

    fun joinRoom(roomId: Long, userId: Long) {
        onlineUserTracker.join(roomId, userId)
    }

    fun leaveRoom(roomId: Long, userId: Long) {
        onlineUserTracker.leave(roomId, userId)
    }

    fun getOnlineUsers(roomId: Long): Set<Long> =
        onlineUserTracker.getOnlineUsers(roomId)
}
