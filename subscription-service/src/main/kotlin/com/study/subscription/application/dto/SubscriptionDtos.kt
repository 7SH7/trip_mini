package com.study.subscription.application.dto

import com.study.subscription.domain.entity.Subscription
import com.study.subscription.domain.entity.SubscriptionStatus
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class SubscriptionResponse(
    val id: Long,
    val userId: Long,
    val videoCallCredits: Int,
    val totalVideoCallsUsed: Int,
    val status: SubscriptionStatus
) {
    companion object {
        fun from(sub: Subscription) = SubscriptionResponse(
            id = sub.id, userId = sub.userId,
            videoCallCredits = sub.videoCallCredits,
            totalVideoCallsUsed = sub.totalVideoCallsUsed,
            status = sub.status
        )
    }
}

data class PurchaseCreditsRequest(
    @field:NotBlank(message = "Payment ID is required")
    val portonePaymentId: String = ""
)

data class UseVideoCallResponse(
    val success: Boolean,
    val remainingCredits: Int,
    val message: String
)

data class SubscriptionCheckResponse(
    val hasCredits: Boolean,
    val remainingCredits: Int
)

data class CreditPurchaseResponse(
    val id: Long,
    val credits: Int,
    val amount: BigDecimal,
    val status: String
)
