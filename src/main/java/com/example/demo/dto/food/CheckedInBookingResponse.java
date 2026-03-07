package com.example.demo.dto.food;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckedInBookingResponse {
    private Long bookingId;
    private List<String> roomNumbers;
    private String checkInDate;
    private String checkOutDate;
}

