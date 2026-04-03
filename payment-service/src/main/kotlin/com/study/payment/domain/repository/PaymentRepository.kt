package com.study.payment.domain.repository

import com.study.payment.domain.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByBookingId(bookingId: Long): Optional<Payment>
    fun findByUserId(userId: Long): List<Payment>
}
