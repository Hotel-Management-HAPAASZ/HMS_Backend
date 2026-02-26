package com.example.demo.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class AdminBookingRequest {

    private Long userId;
    private Long roomId;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private Integer adults;
    private Integer children;

    private String paymentMethod;
    private Double depositAmount; 
}