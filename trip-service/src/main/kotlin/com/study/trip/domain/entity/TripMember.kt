package com.study.trip.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "trip_members", uniqueConstraints = [UniqueConstraint(columnNames = ["trip_id", "user_id"])])
class TripMember(
    @Column(name = "trip_id", nullable = false)
    val tripId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: MemberRole = MemberRole.MEMBER,

    val joinedAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)

enum class MemberRole { OWNER, MEMBER }
