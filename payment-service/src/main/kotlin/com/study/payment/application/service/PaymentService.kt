package com.study.payment.application.service

import com.study.common.event.PaymentCompletedEvent
import com.study.common.event.PaymentFailedEvent
import com.study.common.event.PaymentRefundedEvent
import com.study.common.exception.EntityNotFoundException
import com.study.common.outbox.OutboxEventPublisher
import com.study.payment.application.dto.CreatePaymentRequest
import com.study.payment.application.dto.PaymentResponse
import com.study.payment.domain.entity.Payment
import com.study.payment.domain.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val outboxEventPublisher: OutboxEventPublisher
) {
    @Transactional
    fun createPayment(userId: Long, request: CreatePaymentRequest): PaymentResponse {
        val bookingId = requireNotNull(request.bookingId) { "Booking ID is required" }
        val amount = requireNotNull(request.amount) { "Amount is required" }
        val payment = Payment(bookingId = bookingId, userId = userId, amount = amount)
        val saved = paymentRepository.save(payment)
        return PaymentResponse.from(saved)
    }

    fun getPayment(id: Long): PaymentResponse =
        PaymentResponse.from(paymentRepository.findById(id).orElseThrow { EntityNotFoundException("Payment", id) })

    fun getPaymentsByUser(userId: Long): List<PaymentResponse> =
        paymentRepository.findByUserId(userId).map { PaymentResponse.from(it) }

    @Transactional
    fun completePayment(id: Long): PaymentResponse {
        val payment = paymentRepository.findById(id).orElseThrow { EntityNotFoundException("Payment", id) }
        payment.complete()
        outboxEventPublisher.publish("payment-events", PaymentCompletedEvent(payment.id, payment.bookingId, payment.userId, payment.amount))
        return PaymentResponse.from(payment)
    }

    @Transactional
    fun refundPayment(id: Long): PaymentResponse {
        val payment = paymentRepository.findById(id).orElseThrow { EntityNotFoundException("Payment", id) }
        payment.refund()
        outboxEventPublisher.publish("payment-events", PaymentRefundedEvent(payment.id, payment.bookingId, payment.userId, payment.amount))
        return PaymentResponse.from(payment)
    }

    @Transactional
    fun failPayment(id: Long, reason: String): PaymentResponse {
        val payment = paymentRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Payment", id) }
        payment.fail()
        outboxEventPublisher.publish("payment-events",
            PaymentFailedEvent(payment.id, payment.bookingId, payment.userId, reason))
        return PaymentResponse.from(payment)
    }
}
