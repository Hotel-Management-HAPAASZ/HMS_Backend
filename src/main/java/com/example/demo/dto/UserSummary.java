// com/example/demo/dto/UserSummary.java
package com.example.demo.dto;
import com.example.demo.enums.AccountStatus;
import com.example.demo.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.demo.enums.Department; // ensure this exists

public record UserSummary(
        Long id,
        String userName,
        String email,
         @JsonProperty("phone") String phone,
        UserRole role,
        AccountStatus status,
        Department department // will be null for non-staff users
) {}