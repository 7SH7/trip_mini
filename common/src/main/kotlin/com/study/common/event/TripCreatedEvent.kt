package com.study.common.event

data class TripCreatedEvent(
    val tripId: Long = 0,
    val userId: Long = 0,
    val title: String = ""
) : DomainEvent()
