package com.example.demo.dto.food;

import com.example.demo.enums.FoodOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class FoodOrderResponse {
    private Long orderId;
    private Long bookingId;
    private List<String> roomNumbers;
    private FoodOrderStatus status;
    private Double totalAmount;
    private LocalDateTime expectedDeliveryAt;
    private LocalDateTime createdAt;
    private List<FoodOrderLineResponse> items;

    @Data
    @AllArgsConstructor
    public static class FoodOrderLineResponse {
        private Long foodItemId;
        private String name;
        private Integer quantity;
        private Double unitPrice;
        private Double lineTotal;
    }
}


