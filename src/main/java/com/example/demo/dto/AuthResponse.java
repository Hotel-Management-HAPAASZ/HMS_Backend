package com.example.demo.dto;

import com.example.demo.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

   private Long id;
    private String userName;
    private String email;
    private UserRole role;
    private String token;
    private boolean firstLogin;
    private String phoneNumber;

}
