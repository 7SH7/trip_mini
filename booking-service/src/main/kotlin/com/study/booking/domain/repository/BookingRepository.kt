package com.study.booking.domain.repository

import com.study.booking.domain.entity.Booking
import com.study.booking.domain.entity.BookingStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface BookingRepository : JpaRepository<Booking, Long> {
    fun findByUserId(userId: Long): List<Booking>
    fun findByTripId(tripId: Long): List<Booking>
    fun findByStatusAndCreatedAtBefore(status: BookingStatus, before: LocalDateTime): List<Booking>
}
