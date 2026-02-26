package com.example.demo.dto.billDto;

import lombok.Data;

@Data
public class BillItemRequest {

    private String description;    
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
}