package com.study.trip.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.trip.application.dto.CreateScheduleRequest
import com.study.trip.application.dto.TripScheduleResponse
import com.study.trip.application.dto.UpdateScheduleRequest
import com.study.trip.application.service.TripScheduleService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/trips/{tripId}/schedules")
class TripScheduleController(
    private val scheduleService: TripScheduleService
) {
    @GetMapping
    fun getSchedules(@PathVariable tripId: Long): ApiResponse<List<TripScheduleResponse>> =
        ApiResponse.ok(scheduleService.getSchedules(tripId))

    @PostMapping
    fun create(
        @PathVariable tripId: Long,
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreateScheduleRequest
    ): ApiResponse<TripScheduleResponse> =
        ApiResponse.created(scheduleService.create(tripId, userId, request))

    @PutMapping("/{scheduleId}")
    fun update(
        @PathVariable tripId: Long,
        @PathVariable scheduleId: Long,
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: UpdateScheduleRequest
    ): ApiResponse<TripScheduleResponse> =
        ApiResponse.ok(scheduleService.update(tripId, scheduleId, userId, request))

    @DeleteMapping("/{scheduleId}")
    fun delete(
        @PathVariable tripId: Long,
        @PathVariable scheduleId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<*> {
        scheduleService.delete(tripId, scheduleId, userId)
        return ApiResponse.ok(null)
    }
}
