package com.example.demo.dto;

import com.example.demo.enums.UserRole;
import lombok.Data;

@Data
public class CreateUserRequest {

    private String userName;
    private String email;
    private UserRole role;     
    private String phone;
}