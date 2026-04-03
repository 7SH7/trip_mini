package com.study.subscription.application.service

import com.study.common.exception.EntityNotFoundException
import com.study.common.exception.InvalidRequestException
import com.study.subscription.application.dto.*
import com.study.subscription.domain.entity.CreditPurchase
import com.study.subscription.domain.entity.Subscription
import com.study.subscription.domain.repository.CreditPurchaseRepository
import com.study.subscription.domain.repository.SubscriptionRepository
import com.study.subscription.infrastructure.external.PortOneClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional(readOnly = true)
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val creditPurchaseRepository: CreditPurchaseRepository,
    private val portOneClient: PortOneClient
) {
    companion object {
        const val CREDITS_PER_PURCHASE = 10
        val PRICE_PER_PURCHASE: BigDecimal = BigDecimal(5000)
    }

    @Transactional
    fun getOrCreateSubscription(userId: Long): SubscriptionResponse {
        val subscription = subscriptionRepository.findByUserId(userId)
            .orElseGet {
                subscriptionRepository.save(Subscription(userId = userId))
            }
        return SubscriptionResponse.from(subscription)
    }

    fun getMySubscription(userId: Long): SubscriptionResponse {
        val subscription = subscriptionRepository.findByUserId(userId)
            .orElseThrow { EntityNotFoundException("Subscription", userId) }
        return SubscriptionResponse.from(subscription)
    }

    @Transactional
    fun useVideoCall(userId: Long): UseVideoCallResponse {
        val subscription = subscriptionRepository.findByUserId(userId)
            .orElseGet { subscriptionRepository.save(Subscription(userId = userId)) }

        return if (subscription.useVideoCall()) {
            UseVideoCallResponse(
                success = true,
                remainingCredits = subscription.videoCallCredits,
                message = "영상통화 크레딧을 사용했습니다."
            )
        } else {
            UseVideoCallResponse(
                success = false,
                remainingCredits = 0,
                message = "크레딧이 부족합니다. 충전이 필요합니다."
            )
        }
    }

    @Transactional
    fun purchaseCredits(userId: Long, request: PurchaseCreditsRequest): CreditPurchaseResponse {
        val isValid = portOneClient.verifyPayment(request.portonePaymentId, PRICE_PER_PURCHASE)

        val purchase = CreditPurchase(
            userId = userId,
            credits = CREDITS_PER_PURCHASE,
            amount = PRICE_PER_PURCHASE,
            portonePaymentId = request.portonePaymentId
        )

        if (!isValid) {
            purchase.fail()
            creditPurchaseRepository.save(purchase)
            throw InvalidRequestException("결제 검증에 실패했습니다.")
        }

        purchase.complete()
        creditPurchaseRepository.save(purchase)

        val subscription = subscriptionRepository.findByUserId(userId)
            .orElseGet { subscriptionRepository.save(Subscription(userId = userId)) }
        subscription.addCredits(CREDITS_PER_PURCHASE)

        return CreditPurchaseResponse(
            id = purchase.id,
            credits = purchase.credits,
            amount = purchase.amount,
            status = purchase.status.name
        )
    }

    fun checkCredits(userId: Long): SubscriptionCheckResponse {
        val subscription = subscriptionRepository.findByUserId(userId).orElse(null)
        return if (subscription != null) {
            SubscriptionCheckResponse(
                hasCredits = subscription.hasCredits(),
                remainingCredits = subscription.videoCallCredits
            )
        } else {
            SubscriptionCheckResponse(hasCredits = true, remainingCredits = 5)
        }
    }

    fun getPurchaseHistory(userId: Long): List<CreditPurchaseResponse> {
        return creditPurchaseRepository.findByUserId(userId).map {
            CreditPurchaseResponse(
                id = it.id, credits = it.credits,
                amount = it.amount, status = it.status.name
            )
        }
    }
}
