package com.study.subscription.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "subscriptions")
class Subscription(
    @Column(nullable = false, unique = true)
    val userId: Long,

    @Column(nullable = false)
    var videoCallCredits: Int = 5,

    @Column(nullable = false)
    var totalVideoCallsUsed: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: SubscriptionStatus = SubscriptionStatus.ACTIVE,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    fun useVideoCall(): Boolean {
        if (videoCallCredits <= 0) return false
        videoCallCredits--
        totalVideoCallsUsed++
        updatedAt = LocalDateTime.now()
        return true
    }

    fun addCredits(amount: Int) {
        videoCallCredits += amount
        updatedAt = LocalDateTime.now()
    }

    fun hasCredits(): Boolean = videoCallCredits > 0
}

enum class SubscriptionStatus {
    ACTIVE, SUSPENDED
}
