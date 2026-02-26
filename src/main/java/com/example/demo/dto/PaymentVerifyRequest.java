package com.example.demo.dto;

import lombok.Data;

@Data
public class PaymentVerifyRequest {
    private Long paymentId;
    private String otp;
}