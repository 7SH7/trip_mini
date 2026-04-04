package com.study.trip.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "trip_invite_codes")
class TripInviteCode(
    @Column(nullable = false)
    val tripId: Long,

    @Column(nullable = false, unique = true, length = 8)
    val code: String,

    @Column(nullable = false)
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(7),

    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
}
