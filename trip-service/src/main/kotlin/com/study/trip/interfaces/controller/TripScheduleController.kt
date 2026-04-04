package com.study.trip.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.trip.application.dto.CreateScheduleRequest
import com.study.trip.application.dto.TripScheduleResponse
import com.study.trip.application.dto.UpdateScheduleRequest
import com.study.trip.application.service.TripScheduleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "여행 일정", description = "여행 일정(캘린더) 관리")
@RestController
@RequestMapping("/api/trips/{tripId}/schedules")
class TripScheduleController(
    private val scheduleService: TripScheduleService
) {
    @Operation(summary = "일정 목록 조회")
    @GetMapping
    fun getSchedules(@PathVariable tripId: Long): ApiResponse<List<TripScheduleResponse>> =
        ApiResponse.ok(scheduleService.getSchedules(tripId))

    @Operation(summary = "일정 생성")
    @PostMapping
    fun create(
        @PathVariable tripId: Long,
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreateScheduleRequest
    ): ApiResponse<TripScheduleResponse> =
        ApiResponse.created(scheduleService.create(tripId, userId, request))

    @Operation(summary = "일정 수정")
    @PutMapping("/{scheduleId}")
    fun update(
        @PathVariable tripId: Long,
        @PathVariable scheduleId: Long,
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: UpdateScheduleRequest
    ): ApiResponse<TripScheduleResponse> =
        ApiResponse.ok(scheduleService.update(tripId, scheduleId, userId, request))

    @Operation(summary = "일정 삭제")
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
