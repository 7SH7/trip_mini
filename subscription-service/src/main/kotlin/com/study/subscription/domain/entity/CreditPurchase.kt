package com.study.subscription.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "credit_purchases")
class CreditPurchase(
    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val credits: Int,

    @Column(nullable = false)
    val amount: BigDecimal,

    val portonePaymentId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PurchaseStatus = PurchaseStatus.PENDING,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    var completedAt: LocalDateTime? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    fun complete() {
        status = PurchaseStatus.COMPLETED
        completedAt = LocalDateTime.now()
    }

    fun fail() {
        status = PurchaseStatus.FAILED
    }
}

enum class PurchaseStatus {
    PENDING, COMPLETED, FAILED
}
