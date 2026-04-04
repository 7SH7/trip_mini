package com.study.trip.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.trip.application.dto.*
import com.study.trip.application.service.TripExpenseService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "여행 가계부", description = "지출 내역 및 예산 관리")
@RestController
@RequestMapping("/api/trips/{tripId}/expenses")
class TripExpenseController(
    private val expenseService: TripExpenseService
) {
    @Operation(summary = "지출 목록 조회")
    @GetMapping
    fun getExpenses(@PathVariable tripId: Long): ApiResponse<List<TripExpenseResponse>> =
        ApiResponse.ok(expenseService.getExpenses(tripId))

    @Operation(summary = "지출 요약 조회")
    @GetMapping("/summary")
    fun getSummary(@PathVariable tripId: Long): ApiResponse<ExpenseSummaryResponse> =
        ApiResponse.ok(expenseService.getSummary(tripId))

    @Operation(summary = "지출 등록")
    @PostMapping
    fun create(
        @PathVariable tripId: Long,
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreateExpenseRequest
    ): ApiResponse<TripExpenseResponse> =
        ApiResponse.created(expenseService.create(tripId, userId, request))

    @Operation(summary = "지출 수정")
    @PutMapping("/{expenseId}")
    fun update(
        @PathVariable tripId: Long,
        @PathVariable expenseId: Long,
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: UpdateExpenseRequest
    ): ApiResponse<TripExpenseResponse> =
        ApiResponse.ok(expenseService.update(tripId, expenseId, userId, request))

    @Operation(summary = "지출 삭제")
    @DeleteMapping("/{expenseId}")
    fun delete(
        @PathVariable tripId: Long,
        @PathVariable expenseId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<*> {
        expenseService.delete(tripId, expenseId, userId)
        return ApiResponse.ok(null)
    }

    @Operation(summary = "지출별 멤버 부담금 조회")
    @GetMapping("/{expenseId}/splits")
    fun getSplits(@PathVariable expenseId: Long): ApiResponse<List<ExpenseSplitResponse>> =
        ApiResponse.ok(expenseService.getSplits(expenseId))

    @Operation(summary = "지출별 멤버 부담금 수정 (리더만)")
    @PutMapping("/{expenseId}/splits")
    fun updateSplits(
        @PathVariable tripId: Long,
        @PathVariable expenseId: Long,
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: UpdateSplitRequest
    ): ApiResponse<List<ExpenseSplitResponse>> =
        ApiResponse.ok(expenseService.updateSplits(tripId, expenseId, userId, request))

    @Operation(summary = "최종 정산 결과 (누가 누구에게 얼마 보내야 하는지)")
    @GetMapping("/settlement")
    fun getSettlement(@PathVariable tripId: Long): ApiResponse<SettlementResponse> =
        ApiResponse.ok(expenseService.getSettlement(tripId))
}
