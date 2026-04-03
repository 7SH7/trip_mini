package com.study.booking.application.dto

import jakarta.validation.constraints.NotNull

data class CreateBookingRequest(
    @field:NotNull(message = "Trip ID is required")
    val tripId: Long? = null
)
