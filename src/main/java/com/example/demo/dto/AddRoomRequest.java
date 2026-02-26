package com.example.demo.dto;

import java.util.List;

import com.example.demo.enums.RoomStatus;

import lombok.Data;

@Data
public class AddRoomRequest {
    private String roomNumber;
    private String roomType;
    private Double pricePerNight;
    private RoomStatus status;
    private Integer maxGuest;
    private Integer floor;
    private List<Long> amenityIds;
    private String description;
}