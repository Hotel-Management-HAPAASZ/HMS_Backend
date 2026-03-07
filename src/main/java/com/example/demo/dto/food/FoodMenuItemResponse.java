package com.example.demo.dto.food;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FoodMenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Double price;
    private Boolean available;
}


