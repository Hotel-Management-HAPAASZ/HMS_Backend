package com.example.demo.dto.food;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FoodOrderItemRequest {
    @NotNull
    private Long foodItemId;

    @NotNull
    @Min(1)
    private Integer quantity;
}


