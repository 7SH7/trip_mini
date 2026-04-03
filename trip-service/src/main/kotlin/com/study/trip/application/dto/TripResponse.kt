package com.study.trip.application.dto

import com.study.trip.domain.entity.Trip
import com.study.trip.domain.entity.TripStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class TripResponse(
    val id: Long,
    val userId: Long,
    val title: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: TripStatus,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(trip: Trip) = TripResponse(
            id = trip.id, userId = trip.userId, title = trip.title,
            description = trip.description, startDate = trip.startDate,
            endDate = trip.endDate, status = trip.status, createdAt = trip.createdAt
        )
    }
}
