package com.study.trip.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.trip.application.dto.InviteCodeResponse
import com.study.trip.application.dto.JoinTripRequest
import com.study.trip.application.dto.TripJoinRequestResponse
import com.study.trip.application.dto.TripMemberResponse
import com.study.trip.application.service.TripMemberService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/trips")
class TripMemberController(
    private val memberService: TripMemberService
) {
    @GetMapping("/{tripId}/members")
    fun getMembers(@PathVariable tripId: Long): ApiResponse<List<TripMemberResponse>> =
        ApiResponse.ok(memberService.getMembers(tripId))

    @PostMapping("/{tripId}/members/invite")
    fun generateInviteCode(
        @PathVariable tripId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<InviteCodeResponse> =
        ApiResponse.created(memberService.generateInviteCode(tripId, userId))

    // Saga Step 1: 참여 요청 (PENDING)
    @PostMapping("/join")
    fun requestJoin(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: JoinTripRequest
    ): ApiResponse<TripJoinRequestResponse> =
        ApiResponse.created(memberService.requestJoin(userId, request))

    // Saga Step 2a: 승인 (Confirm)
    @PostMapping("/{tripId}/join-requests/{requestId}/approve")
    fun approveJoinRequest(
        @PathVariable tripId: Long,
        @PathVariable requestId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<TripJoinRequestResponse> =
        ApiResponse.ok(memberService.approveJoinRequest(tripId, requestId, userId))

    // Saga Step 2b: 거절 (Compensate)
    @PostMapping("/{tripId}/join-requests/{requestId}/reject")
    fun rejectJoinRequest(
        @PathVariable tripId: Long,
        @PathVariable requestId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<TripJoinRequestResponse> =
        ApiResponse.ok(memberService.rejectJoinRequest(tripId, requestId, userId))

    @GetMapping("/{tripId}/join-requests")
    fun getPendingRequests(@PathVariable tripId: Long): ApiResponse<List<TripJoinRequestResponse>> =
        ApiResponse.ok(memberService.getPendingRequests(tripId))

    @DeleteMapping("/{tripId}/members/{targetUserId}")
    fun removeMember(
        @PathVariable tripId: Long,
        @PathVariable targetUserId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<*> {
        memberService.removeMember(tripId, targetUserId, userId)
        return ApiResponse.ok(null)
    }
}
