package com.study.payment.application.service;

import com.study.payment.application.dto.CreatePaymentRequest;
import com.study.payment.application.dto.PaymentResponse;
import com.study.payment.domain.entity.Payment;
import com.study.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public PaymentResponse createPayment(Long userId, CreatePaymentRequest request) {
        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .userId(userId)
                .amount(request.getAmount())
                .build();
        Payment saved = paymentRepository.save(payment);
        kafkaTemplate.send("payment-events", "PAYMENT_CREATED:" + saved.getId());
        return PaymentResponse.from(saved);
    }

    public PaymentResponse getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse completePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));
        payment.complete();
        kafkaTemplate.send("payment-events", "PAYMENT_COMPLETED:" + id);
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));
        payment.refund();
        kafkaTemplate.send("payment-events", "PAYMENT_REFUNDED:" + id);
        return PaymentResponse.from(payment);
    }
}
