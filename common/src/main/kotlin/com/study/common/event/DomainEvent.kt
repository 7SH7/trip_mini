package com.study.common.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDateTime
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
abstract class DomainEvent {
    val eventId: String = UUID.randomUUID().toString()
    val occurredAt: LocalDateTime = LocalDateTime.now()
    val eventType: String = this::class.simpleName ?: "Unknown"
}
