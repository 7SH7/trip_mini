package com.study.payment.application.service

import com.study.common.event.DomainEvent
import com.study.common.event.PaymentCompletedEvent
import com.study.common.event.PaymentRefundedEvent
import com.study.common.exception.EntityNotFoundException
import com.study.payment.application.dto.CreatePaymentRequest
import com.study.payment.application.dto.PaymentResponse
import com.study.payment.domain.entity.Payment
import com.study.payment.domain.repository.PaymentRepository
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val kafkaTemplate: KafkaTemplate<String, DomainEvent>
) {
    @Transactional
    fun createPayment(userId: Long, request: CreatePaymentRequest): PaymentResponse {
        val payment = Payment(bookingId = request.bookingId!!, userId = userId, amount = request.amount!!)
        val saved = paymentRepository.save(payment)
        kafkaTemplate.send("payment-events", PaymentCompletedEvent(saved.id, saved.bookingId, saved.userId, saved.amount))
        return PaymentResponse.from(saved)
    }

    fun getPayment(id: Long): PaymentResponse =
        PaymentResponse.from(paymentRepository.findById(id).orElseThrow { EntityNotFoundException("Payment", id) })

    @Transactional
    fun completePayment(id: Long): PaymentResponse {
        val payment = paymentRepository.findById(id).orElseThrow { EntityNotFoundException("Payment", id) }
        payment.complete()
        kafkaTemplate.send("payment-events", PaymentCompletedEvent(payment.id, payment.bookingId, payment.userId, payment.amount))
        return PaymentResponse.from(payment)
    }

    @Transactional
    fun refundPayment(id: Long): PaymentResponse {
        val payment = paymentRepository.findById(id).orElseThrow { EntityNotFoundException("Payment", id) }
        payment.refund()
        kafkaTemplate.send("payment-events", PaymentRefundedEvent(payment.id, payment.bookingId, payment.userId, payment.amount))
        return PaymentResponse.from(payment)
    }
}
