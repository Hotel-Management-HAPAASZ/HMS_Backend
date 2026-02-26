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
    // private Integer numberOfNights;
    private List<String> roomTypes;
    private Integer numberOfGuests;
    private Integer adults;
    private Integer children;

    // Pricing
    private Double baseAmount;
    private Double taxAmount;
    private Double serviceCharges;
    private Double totalAmount;

    // Payment info
    private String paymentMethod;
    private String transactionId;

    private LocalDateTime paidAt;

    private List<RoomDetail> rooms;

    @Data
    public static class RoomDetail {
        private Long roomId;
        private Double roomPrice;
         private String roomType;
        private Integer maxGuest;
    }
}
