package com.example.demo.dto.food;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FoodPaymentVerifyRequest {
    @NotNull
    private Long paymentId;

    @NotBlank
    private String otp;
}


