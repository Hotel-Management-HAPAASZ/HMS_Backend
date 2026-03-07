package com.example.demo.dto.food;

import com.example.demo.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateFoodOrderRequest {

    @NotNull
    private Long userId;

    @NotEmpty
    @Valid
    private List<FoodOrderItemRequest> items;

    // For "Pay now (online)" we will use CARD (OTP verify) for now.
    @NotNull
    private PaymentMethod paymentMethod;

    // Optional: if provided, use this specific booking; otherwise auto-select first checked-in booking
    private Long bookingId;
}


