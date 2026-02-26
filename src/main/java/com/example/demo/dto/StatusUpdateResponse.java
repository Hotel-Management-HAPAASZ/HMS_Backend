package com.example.demo.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusUpdateResponse {
    private Long bookingId;
    private String status;
    private LocalDateTime updatedAt;
}