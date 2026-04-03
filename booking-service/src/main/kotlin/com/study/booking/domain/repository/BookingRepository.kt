package com.study.booking.domain.repository

import com.study.booking.domain.entity.Booking
import org.springframework.data.jpa.repository.JpaRepository

interface BookingRepository : JpaRepository<Booking, Long> {
    fun findByUserId(userId: Long): List<Booking>
    fun findByTripId(tripId: Long): List<Booking>
}
