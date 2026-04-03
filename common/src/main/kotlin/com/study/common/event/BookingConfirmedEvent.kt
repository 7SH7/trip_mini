package com.study.common.event

data class BookingConfirmedEvent(
    val bookingId: Long = 0,
    val userId: Long = 0,
    val tripId: Long = 0
) : DomainEvent()
