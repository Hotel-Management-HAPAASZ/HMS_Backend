package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AddRoomRequest;
import com.example.demo.dto.UpdateRoomRequest;
import com.example.demo.enums.RoomStatus;
import com.example.demo.models.Room;
import com.example.demo.repository.RoomRepository;
import com.example.demo.services.AdminService;
import com.example.demo.services.RoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoomController {

    private final AdminService adminService;
    private final RoomRepository roomRepository;
    private final RoomService roomService;

    /*
     * =====================================================
     * US015 & US016 – ROOM MANAGEMENT
     * =====================================================
     */

    @GetMapping("/rooms")
    public ResponseEntity<Page<Room>> getRooms(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer maxGuest,
            @RequestParam(required = false) List<String> amenities,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "roomNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(
                adminService.getRooms(
                        search, roomType, status,
                        minPrice, maxPrice, maxGuest, amenities,
                        page, size, sortBy, direction));
    }

    @PostMapping("/rooms")
    public ResponseEntity<?> addRoom(@RequestBody AddRoomRequest request) {
        return ResponseEntity.ok(adminService.addRoom(request));
    }

    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<?> updateRoom(
            @PathVariable Long roomId,
            @RequestBody UpdateRoomRequest request) {
        return ResponseEntity.ok(adminService.updateRoom(roomId, request));
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId) {
        adminService.deleteRoom(roomId);
        return ResponseEntity.ok("Room deleted successfully");
    }

    @PostMapping(value = "/rooms/bulk-upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> bulkUploadRooms(
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(defaultValue = "true") boolean upsert,
            @RequestParam(defaultValue = "id") String amenityBy // "id" or "name"
    ) {
        return ResponseEntity.ok(roomService.bulkUploadRooms(file, upsert, amenityBy));
    }

    @GetMapping("/rooms/search")
    public ResponseEntity<?> searchAvailableRooms(
            @RequestParam(required = false) java.time.LocalDate checkInDate,
            @RequestParam(required = false) java.time.LocalDate checkOutDate,
            @RequestParam(defaultValue = "1") int adults,
            @RequestParam(defaultValue = "0") int children,
            @RequestParam String roomType) {
        return ResponseEntity.ok(
                roomService.searchAvailableRooms(
                        checkInDate, checkOutDate, adults, children, roomType));
    }

    @GetMapping("/rooms/{roomId}/prepare-booking")
    public ResponseEntity<?> prepareBooking(
            @PathVariable Long roomId,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate,
            @RequestParam int adults,
            @RequestParam int children) {
        return ResponseEntity.ok(
                roomService.prepareBooking(roomId, checkInDate, checkOutDate, adults, children));
    }

}
