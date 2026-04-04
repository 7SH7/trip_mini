package com.study.trip.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.trip.application.dto.InviteCodeResponse
import com.study.trip.application.dto.JoinTripRequest
import com.study.trip.application.dto.TripJoinRequestResponse
import com.study.trip.application.dto.TripMemberResponse
import com.study.trip.application.service.TripMemberService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "여행 멤버", description = "여행 팟 멤버 관리, 초대 코드, 참여 요청 승인/거절 (보상 트랜잭션)")
@RestController
@RequestMapping("/api/trips")
class TripMemberController(
    private val memberService: TripMemberService
) {
    @Operation(summary = "멤버 목록 조회")
    @GetMapping("/{tripId}/members")
    fun getMembers(@PathVariable tripId: Long): ApiResponse<List<TripMemberResponse>> =
        ApiResponse.ok(memberService.getMembers(tripId))

    @Operation(summary = "초대 코드 생성")
    @PostMapping("/{tripId}/members/invite")
    fun generateInviteCode(
        @PathVariable tripId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<InviteCodeResponse> =
        ApiResponse.created(memberService.generateInviteCode(tripId, userId))

    @Operation(summary = "참여 요청")
    // Saga Step 1: 참여 요청 (PENDING)
    @PostMapping("/join")
    fun requestJoin(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: JoinTripRequest
    ): ApiResponse<TripJoinRequestResponse> =
        ApiResponse.created(memberService.requestJoin(userId, request))

    @Operation(summary = "참여 요청 승인")
    // Saga Step 2a: 승인 (Confirm)
    @PostMapping("/{tripId}/join-requests/{requestId}/approve")
    fun approveJoinRequest(
        @PathVariable tripId: Long,
        @PathVariable requestId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<TripJoinRequestResponse> =
        ApiResponse.ok(memberService.approveJoinRequest(tripId, requestId, userId))

    @Operation(summary = "참여 요청 거절")
    // Saga Step 2b: 거절 (Compensate)
    @PostMapping("/{tripId}/join-requests/{requestId}/reject")
    fun rejectJoinRequest(
        @PathVariable tripId: Long,
        @PathVariable requestId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ApiResponse<TripJoinRequestResponse> =
        ApiResponse.ok(memberService.rejectJoinRequest(tripId, requestId, userId))

    @Operation(summary = "대기 중인 참여 요청 목록 조회")
    @GetMapping("/{tripId}/join-requests")
    fun getPendingRequests(@PathVariable tripId: Long): ApiResponse<List<TripJoinRequestResponse>> =
        ApiResponse.ok(memberService.getPendingRequests(tripId))

    @Operation(summary = "멤버 제거")
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
