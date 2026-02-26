package com.example.demo.dto;

import com.example.demo.enums.UserRole;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String userName;   
    private String phone;
    private String email;
    private UserRole role;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String pincode;
    private String country;
}