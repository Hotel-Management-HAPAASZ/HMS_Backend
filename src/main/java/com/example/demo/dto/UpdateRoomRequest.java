

package com.example.demo.dto;

import java.util.List;

import com.example.demo.enums.RoomStatus;
import lombok.Data;

@Data
public class UpdateRoomRequest {
    private String roomType;
    private Double pricePerNight;
    private RoomStatus status;
    private Integer maxGuest;
    private List<Long> amenityIds; // optional
    private String description;
}