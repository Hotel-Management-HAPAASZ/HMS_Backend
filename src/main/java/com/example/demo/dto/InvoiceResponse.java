// src/main/java/com/example/demo/dto/InvoiceResponse.java
package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceResponse {

    private String invoiceNumber;

    private Long bookingId;

    // Hotel info
    private String hotelName;
    private String hotelAddress;
    private String hotelEmail;
    private String hotelSupportNumber;

    // CustomerInfo
    private String customerName;
    private String customerEmail;
    private String customerMobile;

    // Booking Info
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private List<String> roomTypes;
    private Integer numberOfGuests;
    private Integer adults;
    private Integer children;

    // Pricing
    private Double baseAmount;
    private Double taxAmount;
    private Double serviceCharges; // will be set to 0.0 in service
    private Double totalAmount;

    // Payment info
    private String paymentMethod;
    private String transactionId;

    private LocalDateTime paidAt;


    private List<String> roomNumbers;

    private List<RoomDetail> rooms;

    @Data
    public static class RoomDetail {
        private Long roomId;
        private Double roomPrice;
        private String roomType;
        private Integer maxGuest;
        // NEW: room number for each room line
        private String roomNumber;
    }
}
