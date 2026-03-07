package com.example.demo.models;

import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "food_payments")
@Data
public class FoodPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_order_id", nullable = false)
    private FoodOrder foodOrder;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(unique = true)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime paidAt;

    // For card payments (OTP demo like your booking flow)
    private String otp;
    private LocalDateTime otpGeneratedAt;
}


