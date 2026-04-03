package com.study.payment.application.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class CreatePaymentRequest(
    @field:NotNull(message = "Booking ID is required")
    val bookingId: Long? = null,

    @field:NotNull(message = "Amount is required")
    @field:Positive(message = "Amount must be positive")
    val amount: BigDecimal? = null
)
