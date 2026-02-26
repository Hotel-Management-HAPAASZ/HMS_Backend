package com.example.demo.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class AdminUpdateBookingRequest {

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private Long newRoomId;      
    private Integer adults;
    private Integer children;

    private String specialRequests;
}