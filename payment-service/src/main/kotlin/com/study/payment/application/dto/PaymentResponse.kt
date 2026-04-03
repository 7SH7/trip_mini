package com.study.payment.application.dto

import com.study.payment.domain.entity.Payment
import com.study.payment.domain.entity.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentResponse(
    val id: Long, val bookingId: Long, val userId: Long,
    val amount: BigDecimal, val status: PaymentStatus,
    val paidAt: LocalDateTime?, val createdAt: LocalDateTime
) {
    companion object {
        fun from(payment: Payment) = PaymentResponse(
            id = payment.id, bookingId = payment.bookingId, userId = payment.userId,
            amount = payment.amount, status = payment.status,
            paidAt = payment.paidAt, createdAt = payment.createdAt
        )
    }
}
