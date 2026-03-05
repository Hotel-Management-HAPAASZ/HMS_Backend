package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AdminBookingRow {
    private Long id;
    private Long userId;
    private Long roomId;        // first room id (if multiple); or null
    private String roomName;    // optional convenience
    private String customerName;// optional convenience

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;
    private Double totalAmount;
    private String status;      // CONFIRMED | PENDING | CANCELLED | ...
    private String paymentMethod;
    private Double refundAmount;

    private LocalDateTime createdAt;
}