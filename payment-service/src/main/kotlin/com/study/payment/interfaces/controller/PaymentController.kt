package com.study.payment.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.payment.application.dto.CreatePaymentRequest
import com.study.payment.application.dto.PaymentResponse
import com.study.payment.application.service.PaymentService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping
    fun createPayment(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreatePaymentRequest
    ): ApiResponse<PaymentResponse> = ApiResponse.created(paymentService.createPayment(userId, request))

    @GetMapping("/{id}")
    fun getPayment(@PathVariable id: Long): ApiResponse<PaymentResponse> =
        ApiResponse.ok(paymentService.getPayment(id))

    @PatchMapping("/{id}/complete")
    fun completePayment(@PathVariable id: Long): ApiResponse<PaymentResponse> =
        ApiResponse.ok(paymentService.completePayment(id))

    @PatchMapping("/{id}/refund")
    fun refundPayment(@PathVariable id: Long): ApiResponse<PaymentResponse> =
        ApiResponse.ok(paymentService.refundPayment(id))
}
