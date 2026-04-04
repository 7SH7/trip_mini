package com.study.trip.application.dto

import com.study.trip.domain.entity.TripPlace
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class TripPlaceResponse(
    val id: Long,
    val tripId: Long,
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val category: String?,
    val notes: String?,
    val addedBy: Long?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(p: TripPlace) = TripPlaceResponse(
            p.id, p.tripId, p.name, p.address, p.latitude, p.longitude, p.category, p.notes, p.addedBy, p.createdAt
        )
    }
}

data class CreatePlaceRequest(
    @field:NotBlank val name: String = "",
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val category: String? = null,
    val notes: String? = null
)

data class UpdatePlaceRequest(
    @field:NotBlank val name: String = "",
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val category: String? = null,
    val notes: String? = null
)
