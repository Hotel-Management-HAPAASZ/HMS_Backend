package com.example.demo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="refresh_token")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable =false,unique=true)
    private String token;

    @OneToOne
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    private LocalDateTime expiryDate;

    private boolean revoked;

}



