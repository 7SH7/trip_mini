package com.study.trip.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.trip.application.dto.CreatePlaceRequest
import com.study.trip.application.dto.TripPlaceResponse
import com.study.trip.application.dto.UpdatePlaceRequest
import com.study.trip.application.service.TripPlaceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "여행 장소", description = "방문 장소 관리")
@RestController
@RequestMapping("/api/trips/{tripId}/places")
class TripPlaceController(
    private val placeService: TripPlaceService
) {
    @Operation(summary = "장소 목록 조회")
    @GetMapping
    fun getPlaces(@PathVariable tripId: Long): ApiResponse<List<TripPlaceResponse>> =
        ApiResponse.ok(placeService.getPlaces(tripId))

    @Operation(summary = "장소 추가")
    @PostMapping
    fun create(
        @PathVariable tripId: Long,
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreatePlaceRequest
    ): ApiResponse<TripPlaceResponse> =
        ApiResponse.created(placeService.create(tripId, userId, request))

    @Operation(summary = "장소 수정")
    @PutMapping("/{placeId}")
    fun update(
        @PathVariable tripId: Long,
        @PathVariable placeId: Long,
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: UpdatePlaceRequest
    ): ApiResponse<TripPlaceResponse> =
        ApiResponse.ok(placeService.update(tripId, placeId, userId, request))

    @Operation(summary = "장소 삭제")
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
