package com.study.trip.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.trip.application.dto.CreatePlaceRequest
import com.study.trip.application.dto.TripPlaceResponse
import com.study.trip.application.dto.UpdatePlaceRequest
import com.study.trip.application.service.TripPlaceService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/trips/{tripId}/places")
class TripPlaceController(
    private val placeService: TripPlaceService
) {
    @GetMapping
    fun getPlaces(@PathVariable tripId: Long): ApiResponse<List<TripPlaceResponse>> =
        ApiResponse.ok(placeService.getPlaces(tripId))

    @PostMapping
    fun create(
        @PathVariable tripId: Long,
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreatePlaceRequest
    ): ApiResponse<TripPlaceResponse> =
        ApiResponse.created(placeService.create(tripId, userId, request))

    @PutMapping("/{placeId}")
    fun update(
        @PathVariable tripId: Long,
        @PathVariable placeId: Long,
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: UpdatePlaceRequest
    ): ApiResponse<TripPlaceResponse> =
        ApiResponse.ok(placeService.update(tripId, placeId, userId, request))

    @DeleteMapping("/{placeId}")
    fun delete(
        @PathVariable tripId: Long,
        @PathVariable placeId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<*> {
        placeService.delete(tripId, placeId, userId)
        return ApiResponse.ok(null)
    }
}
