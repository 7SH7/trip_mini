package com.study.trip.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "trip_join_requests")
class TripJoinRequest(
    @Column(nullable = false)
    val tripId: Long,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val inviteCode: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: JoinRequestStatus = JoinRequestStatus.PENDING,

    var processedBy: Long? = null,
    var processedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    fun approve(processedBy: Long) {
        this.status = JoinRequestStatus.APPROVED
        this.processedBy = processedBy
        this.processedAt = LocalDateTime.now()
    }

    fun reject(processedBy: Long) {
        this.status = JoinRequestStatus.REJECTED
        this.processedBy = processedBy
        this.processedAt = LocalDateTime.now()
    }
}

enum class JoinRequestStatus { PENDING, APPROVED, REJECTED }
