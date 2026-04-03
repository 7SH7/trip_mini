package com.study.payment.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class CreatePaymentRequest {
    private Long bookingId;
    private BigDecimal amount;
}
