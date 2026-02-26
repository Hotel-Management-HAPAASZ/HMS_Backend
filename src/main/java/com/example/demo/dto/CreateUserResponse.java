package com.example.demo.dto;

import com.example.demo.enums.UserRole;
import lombok.Data;

@Data
public class CreateUserResponse {
    private Long id;
    private String email;
    private String userName;
    private UserRole role;
    private String tempPassword;  
}
