package com.example.demo.repository;

import com.example.demo.models.Booking;
import com.example.demo.enums.BookingStatus;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends BaseRepository<Booking, Long> ,JpaSpecificationExecutor<Booking>{

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByStatusAndCheckOutDateBefore(BookingStatus status, LocalDate date);

    List<Booking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);

    boolean existsByBookingRooms_Room_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
            Long roomId,
            LocalDate endDate,
            LocalDate startDate);

    boolean existsByBookingRooms_Room_IdAndStatusAndCheckInDateLessThanEqualAndCheckOutDateGreaterThan(
            Long roomId,
            BookingStatus status,
            LocalDate date1,
            LocalDate date2

    );

    @Query("""
                SELECT DISTINCT br.room.id
                FROM Booking b
                JOIN b.bookingRooms br
                WHERE b.status = 'CONFIRMED'
                   AND b.checkInDate <= :date
                   AND b.checkOutDate > :date
            """)
    List<Long> findBookedRoomIdsOnDate(@Param("date") LocalDate date);

    @Query("""
                SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
                FROM Booking b
                JOIN b.bookingRooms br
                WHERE br.room.id = :roomId
                  AND b.checkInDate <= :endDate
                  AND b.checkOutDate >= :startDate
            """)
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("endDate") LocalDate endDate,
            @Param("startDate") LocalDate startDate);

    @Query("""
                SELECT b
                FROM Booking b
                JOIN b.bookingRooms br
                WHERE br.room.id = :roomId
                  AND b.status <> 'CANCELLED'
                  AND (:checkInDate < b.checkOutDate)
                  AND (:checkOutDate > b.checkInDate)
            """)
    List<Booking> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);

    List<Booking> findByUserId(Long userId);

    @Query("""
        SELECT DISTINCT b
        FROM Booking b
        JOIN FETCH b.bookingRooms br
        JOIN FETCH br.room r
        WHERE b.user.id = :userId
          AND b.status IN (com.example.demo.enums.BookingStatus.CHECKED_IN, com.example.demo.enums.BookingStatus.CONFIRMED)
          AND b.checkInDate <= :today
          AND b.checkOutDate >= :today
        ORDER BY b.checkInDate DESC
    """)
    Optional<Booking> findActiveStayBookingForUser(
            @Param("userId") Long userId,
            @Param("today") LocalDate today
    );

    @Query("""
        SELECT DISTINCT b
        FROM Booking b
        JOIN FETCH b.bookingRooms br
        JOIN FETCH br.room r
        WHERE b.user.id = :userId
          AND b.status IN (com.example.demo.enums.BookingStatus.CHECKED_IN, com.example.demo.enums.BookingStatus.CONFIRMED)
          AND b.checkInDate <= :today
          AND b.checkOutDate >= :today
        ORDER BY b.checkInDate DESC
    """)
    List<Booking> findAllActiveStayBookingsForUser(
            @Param("userId") Long userId,
            @Param("today") LocalDate today
    );

    long countByCheckInDate(LocalDate date);

    long countByCheckInDateBetween(LocalDate start, LocalDate end);

    boolean existsByBookingRooms_Room_IdAndCheckOutDateAfter(
            Long roomId,
            LocalDate date);

    @Query("""
        SELECT b FROM Booking b
        JOIN b.bookingRooms br
        WHERE br.room.id = :roomId
          AND b.status NOT IN (com.example.demo.enums.BookingStatus.CANCELLED, com.example.demo.enums.BookingStatus.CHECKED_OUT)
          AND b.checkOutDate > :date
        ORDER BY b.checkInDate ASC
        LIMIT 1
    """)
    Optional<Booking> findFirstOccupyingBooking(
            @Param("roomId") Long roomId,
            @Param("date") LocalDate date);
}