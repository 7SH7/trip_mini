package com.study.booking.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.booking.application.dto.CreateBookingRequest
import com.study.booking.application.dto.BookingResponse
import com.study.booking.application.service.BookingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "예약", description = "예약 생성, 조회, 확정, 취소")
@RestController
@RequestMapping("/api/bookings")
class BookingController(private val bookingService: BookingService) {

    @Operation(summary = "예약 생성")
    @PostMapping
    fun createBooking(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreateBookingRequest
    ): ApiResponse<BookingResponse> = ApiResponse.created(bookingService.createBooking(userId, request))

    @Operation(summary = "예약 단건 조회")
    @GetMapping("/{id}")
    fun getBooking(@PathVariable id: Long): ApiResponse<BookingResponse> =
        ApiResponse.ok(bookingService.getBooking(id))

    @Operation(summary = "내 예약 목록 조회")
    @GetMapping("/my")
    fun getMyBookings(@RequestHeader("X-User-Id") userId: Long): ApiResponse<List<BookingResponse>> =
        ApiResponse.ok(bookingService.getBookingsByUser(userId))

    @Operation(summary = "예약 확정")
    @PatchMapping("/{id}/confirm")
    fun confirmBooking(@PathVariable id: Long): ApiResponse<BookingResponse> =
        ApiResponse.ok(bookingService.confirmBooking(id))

    @Operation(summary = "예약 취소")
    @PatchMapping("/{id}/cancel")
    fun cancelBooking(@PathVariable id: Long): ApiResponse<BookingResponse> =
        ApiResponse.ok(bookingService.cancelBooking(id))
}
