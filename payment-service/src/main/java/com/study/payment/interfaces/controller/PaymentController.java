package com.study.payment.interfaces.controller;

import com.study.common.dto.ApiResponse;
import com.study.payment.application.dto.CreatePaymentRequest;
import com.study.payment.application.dto.PaymentResponse;
import com.study.payment.application.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        return ApiResponse.created(paymentService.createPayment(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getPayment(@PathVariable Long id) {
        return ApiResponse.ok(paymentService.getPayment(id));
    }

    @PatchMapping("/{id}/complete")
    public ApiResponse<PaymentResponse> completePayment(@PathVariable Long id) {
        return ApiResponse.ok(paymentService.completePayment(id));
    }

    @PatchMapping("/{id}/refund")
    public ApiResponse<PaymentResponse> refundPayment(@PathVariable Long id) {
        return ApiResponse.ok(paymentService.refundPayment(id));
    }
}
