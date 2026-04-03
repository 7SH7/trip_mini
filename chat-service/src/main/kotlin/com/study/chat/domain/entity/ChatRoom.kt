package com.study.chat.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat_rooms")
class ChatRoom(
    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val centerLatitude: Double,

    @Column(nullable = false)
    val centerLongitude: Double,

    @Column(nullable = false)
    val radiusKm: Double = 5.0,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
