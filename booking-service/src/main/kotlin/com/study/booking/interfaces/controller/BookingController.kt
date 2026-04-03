package com.study.booking.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.booking.application.dto.CreateBookingRequest
import com.study.booking.application.dto.BookingResponse
import com.study.booking.application.service.BookingService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bookings")
class BookingController(private val bookingService: BookingService) {

    @PostMapping
    fun createBooking(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreateBookingRequest
    ): ApiResponse<BookingResponse> = ApiResponse.created(bookingService.createBooking(userId, request))

    @GetMapping("/{id}")
    fun getBooking(@PathVariable id: Long): ApiResponse<BookingResponse> =
        ApiResponse.ok(bookingService.getBooking(id))

    @GetMapping("/my")
    fun getMyBookings(@RequestHeader("X-User-Id") userId: Long): ApiResponse<List<BookingResponse>> =
        ApiResponse.ok(bookingService.getBookingsByUser(userId))

    @PatchMapping("/{id}/confirm")
    fun confirmBooking(@PathVariable id: Long): ApiResponse<BookingResponse> =
        ApiResponse.ok(bookingService.confirmBooking(id))

    @PatchMapping("/{id}/cancel")
    fun cancelBooking(@PathVariable id: Long): ApiResponse<BookingResponse> =
        ApiResponse.ok(bookingService.cancelBooking(id))
}
