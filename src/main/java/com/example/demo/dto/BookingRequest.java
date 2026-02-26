package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class BookingRequest {
    private Long userId;
    private List<Long> roomIds;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer numberOfGuests;
    private int adults;
    private int children;
}
