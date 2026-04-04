package com.study.trip.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.trip.application.dto.CreateTripRequest
import com.study.trip.application.dto.TripResponse
import com.study.trip.application.service.TripService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "여행", description = "여행 CRUD")
@RestController
@RequestMapping("/api/trips")
class TripController(
    private val tripService: TripService
) {
    @Operation(summary = "여행 생성")
    @PostMapping
    fun createTrip(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreateTripRequest
    ): ApiResponse<TripResponse> = ApiResponse.created(tripService.createTrip(userId, request))

    @Operation(summary = "여행 단건 조회")
    @GetMapping("/{id}")
    fun getTrip(@PathVariable id: Long): ApiResponse<TripResponse> =
        ApiResponse.ok(tripService.getTrip(id))

    @Operation(summary = "내 여행 목록 조회")
    @GetMapping("/my")
    fun getMyTrips(@RequestHeader("X-User-Id") userId: Long): ApiResponse<List<TripResponse>> =
        ApiResponse.ok(tripService.getTripsByUser(userId))

    @Operation(summary = "여행 검색")
    @GetMapping("/search")
    fun searchTrips(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) startDateFrom: String?,
        @RequestParam(required = false) startDateTo: String?
    ): ApiResponse<List<TripResponse>> = ApiResponse.ok(tripService.searchTrips(
        userId = userId,
        status = status?.let { com.study.trip.domain.entity.TripStatus.valueOf(it) },
        keyword = keyword,
        startDateFrom = startDateFrom?.let { java.time.LocalDate.parse(it) },
        startDateTo = startDateTo?.let { java.time.LocalDate.parse(it) }
    ))
}
