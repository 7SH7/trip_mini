package com.study.booking.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "bookings")
class Booking(
    @Column(nullable = false) val userId: Long,
    @Column(nullable = false) val tripId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: BookingStatus = BookingStatus.PENDING,

    val bookedAt: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    fun confirm() { status = BookingStatus.CONFIRMED; updatedAt = LocalDateTime.now() }
    fun cancel() { status = BookingStatus.CANCELLED; updatedAt = LocalDateTime.now() }
}
