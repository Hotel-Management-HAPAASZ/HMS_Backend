package com.example.demo.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.AddRoomRequest;
import com.example.demo.dto.BulkRoomUploadResult;
import com.example.demo.dto.RoomSearchResponse;
import com.example.demo.dto.UpdateRoomRequest;
import com.example.demo.enums.RoomStatus;
import com.example.demo.models.Amenity;
import com.example.demo.models.Room;
import com.example.demo.repository.AmenityRepository;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.sepcification.RoomSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final AmenityRepository amenityRepository;

    // ------------------------------
    // GET ROOMS
    // ------------------------------
    public Page<Room> getRooms(
            String search,
            String roomType,
            RoomStatus status,
            Double minPrice,
            Double maxPrice,
            Integer maxGuest,
            List<String> amenities,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (search != null && !search.isBlank()) {
            return roomRepository.search(search, pageable);
        }

        Specification<Room> spec = RoomSpecification.filter(
                roomType,
                status,
                minPrice,
                maxPrice,
                maxGuest,
                amenities);

        return roomRepository.findAll(spec, pageable);
    }

    // ------------------------------
    // ADD ROOM
    // ------------------------------
    public Room addRoom(AddRoomRequest request) {

        if (request.getPricePerNight() <= 0) {
            throw new RuntimeException("Price must be greater than zero");
        }

        if (request.getMaxGuest() <= 0) {
            throw new RuntimeException("Max occupancy must be positive");
        }

        Room room = new Room();
        room.setRoomNumber(generateRoomNumber());
        room.setRoomType(request.getRoomType());
        room.setStatus(request.getStatus() != null ? request.getStatus() : RoomStatus.AVAILABLE);
        room.setMaxGuest(request.getMaxGuest());
        room.setPricePerNight(request.getPricePerNight());
        room.setCreatedAt(LocalDateTime.now());

        // Amenities
        if (request.getAmenityIds() != null && !request.getAmenityIds().isEmpty()) {
            var amenities = amenityRepository.findAllById(request.getAmenityIds());
            room.setAmenities(new HashSet<>(amenities));
        }

        return roomRepository.save(room);
    }

    // ------------------------------
    // UPDATE ROOM
    // ------------------------------
    public Room updateRoom(Long roomId, UpdateRoomRequest request) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        boolean hasActiveBooking = bookingRepository
                .existsByBookingRooms_Room_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                        roomId, LocalDate.now(), LocalDate.now());

        if (hasActiveBooking) {
            throw new RuntimeException("Room has active/upcoming reservations");
        }

        if (request.getPricePerNight() != null && request.getPricePerNight() <= 0) {
            throw new RuntimeException("Invalid price");
        }

        if (request.getRoomType() != null)
            room.setRoomType(request.getRoomType());
        if (request.getStatus() != null)
            room.setStatus(request.getStatus());
        if (request.getMaxGuest() != null)
            room.setMaxGuest(request.getMaxGuest());
        if (request.getPricePerNight() != null)
            room.setPricePerNight(request.getPricePerNight());

        if (request.getAmenityIds() != null && !request.getAmenityIds().isEmpty()) {
            var amenities = amenityRepository.findAllById(request.getAmenityIds());
            room.setAmenities(new HashSet<>(amenities));
        }

        room.setUpdatedAt(LocalDateTime.now());
        return roomRepository.save(room);
    }

    // ------------------------------
    // DELETE ROOM
    // ------------------------------
    public void deleteRoom(Long roomId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        boolean hasAnyBooking = bookingRepository
                .existsByBookingRooms_Room_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                        roomId, LocalDate.now(), LocalDate.now().plusYears(10));

        if (hasAnyBooking) {
            throw new RuntimeException("Room cannot be deleted because it has booking history");
        }

        roomRepository.delete(room);
    }

    // ------------------------------
    // SEARCH AVAILABLE ROOMS
    // ------------------------------
    public List<RoomSearchResponse> searchAvailableRooms(
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int adults,
            int children,
            String roomType) {

        if (checkInDate == null || checkOutDate == null)
            throw new RuntimeException("Check-in and Check-out dates are required");

        if (checkOutDate.isBefore(checkInDate))
            throw new RuntimeException("Check-out must be after check-in");

        if (adults < 1)
            throw new RuntimeException("At least 1 adult required");

        if (!StringUtils.hasText(roomType))
            throw new RuntimeException("Room type required");

        int totalGuests = adults + children;

        var rooms = roomRepository.findAvailableRooms(checkInDate, checkOutDate, roomType, totalGuests);

        if (rooms.isEmpty())
            throw new RuntimeException("No rooms found");

        return rooms.stream().map(room -> {
            RoomSearchResponse dto = new RoomSearchResponse();
            dto.setRoomId(room.getId());
            dto.setRoomType(room.getRoomType());
            dto.setRoomNumber(room.getRoomNumber());
            dto.setPricePerNight(room.getPricePerNight());
            dto.setMaxGuest(room.getMaxGuest());
            dto.setAvailabilityStatus(room.getStatus().toString());
            dto.setAmenities(room.getAmenities().stream().map(Amenity::getName).collect(Collectors.toList()));
            dto.setImageUrl("https://hotel.com/images/" + room.getId());
            return dto;
        }).collect(Collectors.toList());
    }

    // ------------------------------
    // BULK CSV UPLOAD (CLEAN VERSION)
    // ------------------------------
    @Transactional
    public BulkRoomUploadResult bulkUploadRooms(MultipartFile file, boolean upsert, String amenityBy) {

        BulkRoomUploadResult result = new BulkRoomUploadResult();
        result.setUpsert(upsert);

        if (file == null || file.isEmpty()) {
            result.getErrors().add(new BulkRoomUploadResult.RowError(0, null, "File is empty"));
            return result;
        }

        try {

            CSVParser parser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)));

            List<CSVRecord> records = parser.getRecords();
            result.setTotalRows(records.size());

            Map<Long, Amenity> amenityById =
                    amenityRepository.findAll().stream().collect(Collectors.toMap(Amenity::getId, a -> a));

            for (int i=0; i<records.size(); i++) {

                CSVRecord rec = records.get(i);
                int row = i + 2;

                try {
                    String roomNumber = rec.get("roomNumber").trim();
                    String roomType = rec.get("roomType").trim();
                    double price = Double.parseDouble(rec.get("pricePerNight").trim());
                    int maxGuest = Integer.parseInt(rec.get("maxGuest").trim());

                    if (roomNumber.isBlank()) {
                        roomNumber = "RM" + System.currentTimeMillis();
                    }

                    Room room = roomRepository.findByRoomNumber(roomNumber).orElse(new Room());
                    boolean isUpdate = room.getId() != null;

                    room.setRoomNumber(roomNumber);
                    room.setRoomType(roomType);
                    room.setPricePerNight(price);
                    room.setMaxGuest(maxGuest);
                    room.setStatus(RoomStatus.AVAILABLE);

                    String amenitiesStr = rec.get("amenities");
                    if (amenitiesStr != null && !amenitiesStr.isBlank()) {
                        Set<Amenity> set = new HashSet<>();
                        for (String idStr : amenitiesStr.split(";")) {
                            Long id = Long.parseLong(idStr.trim());
                            if (amenityById.containsKey(id)) {
                                set.add(amenityById.get(id));
                            }
                        }
                        room.setAmenities(set);
                    }

                    roomRepository.save(room);

                    if (isUpdate) result.setUpdatedCount(result.getUpdatedCount() + 1);
                    else result.getCreatedRoomIds().add(room.getId());

                } catch (Exception e) {
                    result.getErrors().add(new BulkRoomUploadResult.RowError(row, null, e.getMessage()));
                }
            }

            result.setSuccessCount(result.getCreatedRoomIds().size() + result.getUpdatedCount());
            result.setFailureCount(result.getErrors().size());
            return result;

        } catch (Exception e) {
            result.getErrors().add(new BulkRoomUploadResult.RowError(0, null, e.getMessage()));
            return result;
        }
    }

     public Object prepareBooking(Long roomId, LocalDate checkIn, LocalDate checkOut, int adults, int children) {

        return new Object() {
            public final Long selectedRoom = roomId;
            public final LocalDate checkInDate = checkIn;
            public final LocalDate checkOutDate = checkOut;
            public final int totalAdults = adults;
            public final int totalChildren = children;
            public final String message = "Booking data pre-filled successfully.";
        };
    }
    // ------------------------------
    private String generateRoomNumber() {
        return "RM" + System.currentTimeMillis();
    }
}