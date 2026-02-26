package com.example.demo.dto;
import java.util.List;

import lombok.Data;

@Data
public class RoomSearchResponse {

    private Long roomId;
    private String roomType;
    private String roomNumber;
    private Double pricePerNight;
    private Integer maxGuest;
    private String availabilityStatus;
    private List<String> amenities;
    private String imageUrl;     
}
