package com.example.demo.dto;

import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    
    private Long paymentId;
    private Long bookingId;
    private Double amount;

    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    private String message;
    private LocalDateTime paidAt;
}
