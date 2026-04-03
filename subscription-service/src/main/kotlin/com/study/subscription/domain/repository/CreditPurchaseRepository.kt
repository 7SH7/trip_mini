package com.study.subscription.domain.repository

import com.study.subscription.domain.entity.CreditPurchase
import org.springframework.data.jpa.repository.JpaRepository

interface CreditPurchaseRepository : JpaRepository<CreditPurchase, Long> {
    fun findByUserId(userId: Long): List<CreditPurchase>
}
