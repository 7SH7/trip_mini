package com.study.common.outbox

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "outbox_events")
class OutboxEvent(
    @Column(nullable = false)
    val topic: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val payload: String,

    @Column(nullable = false)
    val eventType: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OutboxStatus = OutboxStatus.PENDING,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    var publishedAt: LocalDateTime? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    fun markPublished() {
        status = OutboxStatus.PUBLISHED
        publishedAt = LocalDateTime.now()
    }

    fun markFailed() {
        status = OutboxStatus.FAILED
    }
}

enum class OutboxStatus {
    PENDING, PUBLISHED, FAILED
}
