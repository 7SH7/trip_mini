package com.study.subscription.domain.repository

import com.study.subscription.domain.entity.Subscription
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    fun findByUserId(userId: Long): Optional<Subscription>
}
