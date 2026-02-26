package com.example.demo.dto;

import com.example.demo.enums.UserRole;
import lombok.Data;

@Data
public class CreateUserWithStaffRequest {
    private CreateUserRequest user;    
    private String departmentName;
}