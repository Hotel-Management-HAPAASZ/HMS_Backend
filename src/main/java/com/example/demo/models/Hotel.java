// package com.example.demo.models;

// import java.time.LocalDateTime;
// import jakarta.persistence.*;
// import lombok.*;

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// @Entity
// @Table(name = "hotels")
// public class Hotel {

//     @Id
//     @GeneratedValue(strategy =GenerationType.IDENTITY)
//     private Long id;

//     @Column(nullable = false)
//     // private String name;
     
//     private String description;
//     private String city;

//     private Double rating;

//     @Column(nullable = false)
//     private Boolean active;

//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name="owner_id",nullable = false)
//     private User owner;

//     private LocalDateTime createdAt;

//     private LocalDateTime updatedAt;
    
// }
