package com.example.demo.repository;

import com.example.demo.enums.RoomStatus;
import com.example.demo.models.Room;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends BaseRepository<Room, Long>,JpaSpecificationExecutor<Room>  {

    Optional<Room> findByRoomNumber(String roomNumber);
    List<Room> findByRoomNumberIn(Collection<String> roomNumbers);
    

    List<Room> findByRoomType(String roomType);


    // List<Room> findByHotelId(Long hotelId);
  
@Query("""
        SELECT r FROM Room r
        WHERE LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(r.roomType) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Room> search(@Param("keyword") String keyword, Pageable pageable);


    long countByStatus(RoomStatus status);
   
List<Room> findByRoomTypeAndStatusAndMaxGuestGreaterThanEqual(
            String roomType,
            RoomStatus status,
            Integer maxGuest
    );
 @Query("""
    SELECT r FROM Room r
    WHERE r.roomType = :roomType
      AND r.maxGuest >= :totalGuests
      AND r.status = com.example.demo.enums.RoomStatus.AVAILABLE
      AND r.id NOT IN (
          SELECT br.room.id 
          FROM BookingRoom br
          JOIN br.booking b
          WHERE b.checkInDate <= :checkOut
            AND b.checkOutDate >= :checkIn
      )
""")
List<Room> findAvailableRooms(
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut,
        @Param("roomType") String roomType,
        @Param("totalGuests") Integer totalGuests
);

}
