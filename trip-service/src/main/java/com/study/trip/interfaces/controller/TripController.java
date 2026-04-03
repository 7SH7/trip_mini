package com.study.trip.interfaces.controller;

import com.study.common.dto.ApiResponse;
import com.study.trip.application.dto.CreateTripRequest;
import com.study.trip.application.dto.TripResponse;
import com.study.trip.application.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ApiResponse<TripResponse> createTrip(@RequestHeader("X-User-Id") Long userId,
                                                @RequestBody CreateTripRequest request) {
        return ApiResponse.created(tripService.createTrip(userId, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TripResponse> getTrip(@PathVariable Long id) {
        return ApiResponse.ok(tripService.getTrip(id));
    }

    @GetMapping("/my")
    public ApiResponse<List<TripResponse>> getMyTrips(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.ok(tripService.getTripsByUser(userId));
    }
}
