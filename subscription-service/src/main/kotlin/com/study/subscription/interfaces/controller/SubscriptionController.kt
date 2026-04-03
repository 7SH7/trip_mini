package com.study.subscription.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.subscription.application.dto.*
import com.study.subscription.application.service.SubscriptionService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionController(
    private val subscriptionService: SubscriptionService
) {
    @GetMapping("/my")
    fun getMySubscription(@RequestHeader("X-User-Id") userId: Long): ApiResponse<SubscriptionResponse> =
        ApiResponse.ok(subscriptionService.getOrCreateSubscription(userId))

    @PostMapping("/video-call/use")
    fun useVideoCall(@RequestHeader("X-User-Id") userId: Long): ApiResponse<UseVideoCallResponse> =
        ApiResponse.ok(subscriptionService.useVideoCall(userId))

    @PostMapping("/credits/purchase")
    fun purchaseCredits(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: PurchaseCreditsRequest
    ): ApiResponse<CreditPurchaseResponse> =
        ApiResponse.ok(subscriptionService.purchaseCredits(userId, request))

    @GetMapping("/credits/history")
    fun getPurchaseHistory(@RequestHeader("X-User-Id") userId: Long): ApiResponse<List<CreditPurchaseResponse>> =
        ApiResponse.ok(subscriptionService.getPurchaseHistory(userId))

    @GetMapping("/check")
    fun checkCredits(@RequestParam userId: Long): ApiResponse<SubscriptionCheckResponse> =
        ApiResponse.ok(subscriptionService.checkCredits(userId))
}
