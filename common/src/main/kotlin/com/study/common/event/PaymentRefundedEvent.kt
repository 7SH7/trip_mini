package com.study.common.event

import java.math.BigDecimal

data class PaymentRefundedEvent(
    val paymentId: Long = 0,
    val bookingId: Long = 0,
    val userId: Long = 0,
    val amount: BigDecimal = BigDecimal.ZERO
) : DomainEvent()
