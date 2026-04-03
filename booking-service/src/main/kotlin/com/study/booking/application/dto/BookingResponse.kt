package com.study.booking.application.dto

import com.study.booking.domain.entity.Booking
import com.study.booking.domain.entity.BookingStatus
import java.time.LocalDateTime

data class BookingResponse(
    val id: Long, val userId: Long, val tripId: Long,
    val status: BookingStatus, val bookedAt: LocalDateTime, val createdAt: LocalDateTime
) {
    companion object {
        fun from(booking: Booking) = BookingResponse(
            id = booking.id, userId = booking.userId, tripId = booking.tripId,
            status = booking.status, bookedAt = booking.bookedAt, createdAt = booking.createdAt
        )
    }
}
