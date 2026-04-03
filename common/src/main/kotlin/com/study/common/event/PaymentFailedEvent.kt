package com.study.common.event

data class PaymentFailedEvent(
    val paymentId: Long = 0,
    val bookingId: Long = 0,
    val userId: Long = 0,
    val reason: String = ""
) : DomainEvent()
