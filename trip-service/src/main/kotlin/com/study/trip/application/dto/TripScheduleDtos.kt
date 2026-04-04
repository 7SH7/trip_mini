package com.study.trip.application.dto

import com.study.trip.domain.entity.TripSchedule
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class TripScheduleResponse(
    val id: Long,
    val tripId: Long,
    val date: LocalDate,
    val title: String,
    val memo: String?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val orderIndex: Int,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(s: TripSchedule) = TripScheduleResponse(
            s.id, s.tripId, s.date, s.title, s.memo, s.startTime, s.endTime, s.orderIndex, s.createdAt
        )
    }
}

data class CreateScheduleRequest(
    @field:NotNull val date: LocalDate? = null,
    @field:NotBlank val title: String = "",
    val memo: String? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val orderIndex: Int = 0
)

data class UpdateScheduleRequest(
    @field:NotNull val date: LocalDate? = null,
    @field:NotBlank val title: String = "",
    val memo: String? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val orderIndex: Int = 0
)
