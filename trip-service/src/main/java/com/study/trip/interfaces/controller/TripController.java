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
    public ApiResponse<TripResponse> createTrip(@RequestBody CreateTripRequest request) {
        return ApiResponse.created(tripService.createTrip(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TripResponse> getTrip(@PathVariable Long id) {
        return ApiResponse.ok(tripService.getTrip(id));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<TripResponse>> getTripsByUser(@PathVariable Long userId) {
        return ApiResponse.ok(tripService.getTripsByUser(userId));
    }
}
