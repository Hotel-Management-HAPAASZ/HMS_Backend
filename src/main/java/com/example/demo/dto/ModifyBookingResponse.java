package com.example.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModifyBookingResponse {

    private String message;
    private Double amountDifference;
    private Long paymentId; 

    public ModifyBookingResponse(String message, Double amountDifference){

        this.message = message;
        this.amountDifference = amountDifference;
    }
}