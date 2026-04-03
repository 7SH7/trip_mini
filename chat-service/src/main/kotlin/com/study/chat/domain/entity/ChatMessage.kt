package com.study.chat.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat_messages")
class ChatMessage(
    @Column(nullable = false)
    val chatRoomId: Long,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MessageType = MessageType.TEXT,

    val sentAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)

enum class MessageType {
    TEXT, IMAGE, SYSTEM
}
