package com.study.trip.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class CreateTripRequest(
    @field:NotBlank(message = "Title is required")
    val title: String = "",

    val description: String? = null,

    @field:NotNull(message = "Start date is required")
    val startDate: LocalDate? = null,

    @field:NotNull(message = "End date is required")
    val endDate: LocalDate? = null
)
