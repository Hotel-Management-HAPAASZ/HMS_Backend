package com.example.demo.models;

import com.example.demo.enums.AccountStatus;
import com.example.demo.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userName;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    private String phoneNumber;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private UserRole role;
    private boolean firstLogin;
    @Enumerated(EnumType.STRING)
    private Expertise expertise;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    private String otpCode;
    private LocalDateTime otpExpiresAt;
    private Integer otpAttempts;

    private String resetToken;
    private LocalDateTime resetTokenExpiresAt;

    private LocalDateTime updatedAt;
    private LocalDateTime deactivatedAt;
}