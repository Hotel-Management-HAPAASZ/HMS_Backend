package com.example.demo.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="user_id",nullable = false,unique=true)
    private User user;

    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String country;

    private LocalDateTime dateOfBirth;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    
}
