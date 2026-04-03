package com.study.trip.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.trip.application.dto.CreateTripRequest
import com.study.trip.application.dto.TripResponse
import com.study.trip.application.service.TripService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/trips")
class TripController(
    private val tripService: TripService
) {
    @PostMapping
    fun createTrip(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreateTripRequest
    ): ApiResponse<TripResponse> = ApiResponse.created(tripService.createTrip(userId, request))

    @GetMapping("/{id}")
    fun getTrip(@PathVariable id: Long): ApiResponse<TripResponse> =
        ApiResponse.ok(tripService.getTrip(id))

    @GetMapping("/my")
    fun getMyTrips(@RequestHeader("X-User-Id") userId: Long): ApiResponse<List<TripResponse>> =
        ApiResponse.ok(tripService.getTripsByUser(userId))
}
