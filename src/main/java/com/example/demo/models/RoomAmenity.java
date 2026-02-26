// package com.example.demo.models;

// import jakarta.persistence.*;
// import lombok.*;

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// @Entity
// @Table(name = "room_amenities")
// public class RoomAmenity {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @ManyToOne
//     @JoinColumn(name = "room_id", nullable = false)
//     private Room room;

//     @ManyToOne
//     @JoinColumn(name = "amenity_id", nullable = false)
//     private Amenity amenity;
// }