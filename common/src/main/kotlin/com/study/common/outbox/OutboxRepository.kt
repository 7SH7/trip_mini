package com.study.common.outbox

import org.springframework.data.jpa.repository.JpaRepository

interface OutboxRepository : JpaRepository<OutboxEvent, Long> {
    fun findByStatusOrderByCreatedAt(status: OutboxStatus): List<OutboxEvent>
}
