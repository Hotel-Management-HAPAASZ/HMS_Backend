package com.example.demo.dto;

import lombok.Data;
import com.example.demo.enums.PaymentMethod;

@Data
public class PaymentStartRequest {
    
    private Long bookingId;
    private PaymentMethod paymentMethod;

    // if card
    private String cardNumber;
    private String cardHolderName;
    private String expiry;
    private String cvv;
}