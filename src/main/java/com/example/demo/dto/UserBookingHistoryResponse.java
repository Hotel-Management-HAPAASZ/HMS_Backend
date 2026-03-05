package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
@Data
public class UserBookingHistoryResponse {

    private Long bookingId;
    private List<String> roomTypes;
    private List<String> roomNumbers;
    private LocalDate checkIn;
    private LocalDate checkOut;

    private Integer numberOfGuests;
    private String bookingStatus;
    private Double totalAmount;
    private String paymentStatus;
}
