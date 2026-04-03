package com.study.payment.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
class Payment(
    @Column(nullable = false) val bookingId: Long,
    @Column(nullable = false) val userId: Long,

    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,

    var paidAt: LocalDateTime? = null,
    var refundRetryCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    fun complete() { status = PaymentStatus.COMPLETED; paidAt = LocalDateTime.now(); updatedAt = LocalDateTime.now() }
    fun fail() { status = PaymentStatus.FAILED; updatedAt = LocalDateTime.now() }
    fun refund() { status = PaymentStatus.REFUNDED; updatedAt = LocalDateTime.now() }

    fun incrementRetry(): Boolean {
        refundRetryCount++
        return refundRetryCount <= MAX_RETRY

    }

    companion object {
        const val MAX_RETRY = 3
    }
}
