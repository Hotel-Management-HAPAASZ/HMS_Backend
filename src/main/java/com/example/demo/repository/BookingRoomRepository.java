package com.example.demo.repository;

import com.example.demo.models.BookingRoom;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;




public interface BookingRoomRepository extends JpaRepository<BookingRoom, Long> {

    @Query("""
            SELECT COUNT(br) > 0
            FROM BookingRoom br
            WHERE br.room.id = :roomId
            AND br.booking.status <> 'CANCELLED'
            AND br.booking.checkInDate < :checkOutDate
            AND br.booking.checkOutDate > :checkInDate
            """)
    boolean existsOverlappingBooking(
        @Param("roomId") Long roomId,
        @Param("checkInDate") LocalDate checkInDate,
        @Param("checkOutDate") LocalDate checkOutDate
    );

    @Query("""
            SELECT COUNT(br) > 0 
            FROM BookingRoom br
            WHERE br.room.id = :roomId
            AND br.booking.id <> :bookingId
            AND br.booking.status <> 'CANCELLED'
            AND br.booking.checkInDate < :checkOut
            AND br.booking.checkOutDate > :checkIn
            """)
    boolean existsOverlappingBookingForModify(
        @Param("roomId") Long roomId,
        @Param("bookingId") Long bookingId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );
}
    

