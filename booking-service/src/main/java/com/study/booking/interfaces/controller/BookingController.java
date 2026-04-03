package com.study.booking.interfaces.controller;

import com.study.common.dto.ApiResponse;
import com.study.booking.application.dto.CreateBookingRequest;
import com.study.booking.application.dto.BookingResponse;
import com.study.booking.application.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ApiResponse<BookingResponse> createBooking(@RequestHeader("X-User-Id") Long userId,
                                                      @RequestBody CreateBookingRequest request) {
        return ApiResponse.created(bookingService.createBooking(userId, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingResponse> getBooking(@PathVariable Long id) {
        return ApiResponse.ok(bookingService.getBooking(id));
    }

    @GetMapping("/my")
    public ApiResponse<List<BookingResponse>> getMyBookings(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.ok(bookingService.getBookingsByUser(userId));
    }

    @PatchMapping("/{id}/confirm")
    public ApiResponse<BookingResponse> confirmBooking(@PathVariable Long id) {
        return ApiResponse.ok(bookingService.confirmBooking(id));
    }
}
