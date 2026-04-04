package com.study.trip.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.trip.application.dto.*
import com.study.trip.application.service.TripExpenseService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/trips/{tripId}/expenses")
class TripExpenseController(
    private val expenseService: TripExpenseService
) {
    @GetMapping
    fun getExpenses(@PathVariable tripId: Long): ApiResponse<List<TripExpenseResponse>> =
        ApiResponse.ok(expenseService.getExpenses(tripId))

    @GetMapping("/summary")
    fun getSummary(@PathVariable tripId: Long): ApiResponse<ExpenseSummaryResponse> =
        ApiResponse.ok(expenseService.getSummary(tripId))

    @PostMapping
    fun create(
        @PathVariable tripId: Long,
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreateExpenseRequest
    ): ApiResponse<TripExpenseResponse> =
        ApiResponse.created(expenseService.create(tripId, userId, request))

    @PutMapping("/{expenseId}")
    fun update(
        @PathVariable tripId: Long,
        @PathVariable expenseId: Long,
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: UpdateExpenseRequest
    ): ApiResponse<TripExpenseResponse> =
        ApiResponse.ok(expenseService.update(tripId, expenseId, userId, request))

    @DeleteMapping("/{expenseId}")
    fun delete(
        @PathVariable tripId: Long,
        @PathVariable expenseId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<*> {
        expenseService.delete(tripId, expenseId, userId)
        return ApiResponse.ok(null)
    }
}
