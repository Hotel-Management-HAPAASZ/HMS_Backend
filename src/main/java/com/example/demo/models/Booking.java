package com.example.demo.models;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.enums.BookingStatus;
// import com.example.demo.enums.BookingDay;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingRoom> bookingRooms;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = true)
    private Integer adults;

    @Column(nullable = true)
    private Integer children;

    @Column(nullable = true)
    private Integer numberOfGuests;

    @Column(nullable = false)
    private Double amount; 

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    
 @PrePersist
    void onCreate() {
        if (checkOutDate.isBefore(checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
    }

}