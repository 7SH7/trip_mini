package com.study.payment.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.payment.application.dto.CreatePaymentRequest
import com.study.payment.application.dto.PaymentResponse
import com.study.payment.application.service.PaymentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "결제", description = "결제 생성, 완료, 환불, 실패 처리")
@RestController
@RequestMapping("/api/payments")
class PaymentController(private val paymentService: PaymentService) {

    @Operation(summary = "결제 생성")
    @PostMapping
    fun createPayment(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreatePaymentRequest
    ): ApiResponse<PaymentResponse> = ApiResponse.created(paymentService.createPayment(userId, request))

    @Operation(summary = "내 결제 목록 조회")
    @GetMapping("/my")
    fun getMyPayments(@RequestHeader("X-User-Id") userId: Long): ApiResponse<List<PaymentResponse>> =
        ApiResponse.ok(paymentService.getPaymentsByUser(userId))

    @Operation(summary = "결제 단건 조회")
    @GetMapping("/{id}")
    fun getPayment(@PathVariable id: Long): ApiResponse<PaymentResponse> =
        ApiResponse.ok(paymentService.getPayment(id))

    @Operation(summary = "결제 완료 처리")
    @PatchMapping("/{id}/complete")
    fun completePayment(@PathVariable id: Long): ApiResponse<PaymentResponse> =
        ApiResponse.ok(paymentService.completePayment(id))

    @Operation(summary = "결제 환불")
    @PatchMapping("/{id}/refund")
    fun refundPayment(@PathVariable id: Long): ApiResponse<PaymentResponse> =
        ApiResponse.ok(paymentService.refundPayment(id))

    @Operation(summary = "결제 실패 처리")
    @PatchMapping("/{id}/fail")
    fun failPayment(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "Payment failed") reason: String
    ): ApiResponse<PaymentResponse> = ApiResponse.ok(paymentService.failPayment(id, reason))
}
