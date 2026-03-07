package com.example.demo.dto.food;

import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FoodPaymentResponse {
    private Long paymentId;
    private Long orderId;
    private Double amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paidAt;
    private String message;
}


