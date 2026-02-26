package com.example.demo.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="password_reset_token")
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false,unique=true)
    private String token;

    @OneToOne
    @JoinColumn(nullable = false,unique=true)
    private User user;

    private LocalDateTime exipryDate;

    private boolean used;
}
