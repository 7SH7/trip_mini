package com.study.subscription.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.subscription.application.dto.*
import com.study.subscription.application.service.SubscriptionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "구독", description = "영상통화 크레딧 관리, PortOne 결제")
@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionController(
    private val subscriptionService: SubscriptionService
) {
    @Operation(summary = "내 구독 정보 조회")
    @GetMapping("/my")
    fun getMySubscription(@RequestHeader("X-User-Id") userId: Long): ApiResponse<SubscriptionResponse> =
        ApiResponse.ok(subscriptionService.getOrCreateSubscription(userId))

    @Operation(summary = "영상통화 크레딧 사용")
    @PostMapping("/video-call/use")
    fun useVideoCall(@RequestHeader("X-User-Id") userId: Long): ApiResponse<UseVideoCallResponse> =
        ApiResponse.ok(subscriptionService.useVideoCall(userId))

    @Operation(summary = "크레딧 구매 (PortOne 결제)")
    @PostMapping("/credits/purchase")
    fun purchaseCredits(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: PurchaseCreditsRequest
    ): ApiResponse<CreditPurchaseResponse> =
        ApiResponse.ok(subscriptionService.purchaseCredits(userId, request))

    @Operation(summary = "크레딧 구매 이력 조회")
    @GetMapping("/credits/history")
    fun getPurchaseHistory(@RequestHeader("X-User-Id") userId: Long): ApiResponse<List<CreditPurchaseResponse>> =
        ApiResponse.ok(subscriptionService.getPurchaseHistory(userId))

    @Operation(summary = "크레딧 잔여량 확인")
    @GetMapping("/check")
    fun checkCredits(@RequestParam userId: Long): ApiResponse<SubscriptionCheckResponse> =
        ApiResponse.ok(subscriptionService.checkCredits(userId))
}
