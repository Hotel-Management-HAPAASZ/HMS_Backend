//

package com.example.demo.services;

import com.example.demo.dto.*;
import com.example.demo.models.*;
import com.example.demo.repository.*;
import com.example.demo.sepcification.BookingSpecifications;
import com.example.demo.enums.BookingStatus;
import com.example.demo.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingRoomRepository bookingRoomRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {

        if (request.getRoomIds() == null || request.getRoomIds().isEmpty()) {
            throw new RuntimeException("At least one room must be selected");
        }

        if (request.getCheckIn().isAfter(request.getCheckOut())
                || request.getCheckIn().isEqual(request.getCheckOut())) {
            throw new RuntimeException("Invalid stay dates");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        long days = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        if (days <= 0) {
            throw new RuntimeException("Stay must be at least 1 day");
        }

        // Fetch all rooms first
        List<Room> rooms = new ArrayList<>();
        int totalCapacity = 0;

        for (Long roomId : request.getRoomIds()) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
            rooms.add(room);
            totalCapacity += room.getMaxGuest();
        }

        // Compare total capacity with requested guests
        if (request.getNumberOfGuests() > totalCapacity) {
            throw new RuntimeException("Selected rooms cannot accommodate requested guests");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCheckInDate(request.getCheckIn());
        booking.setCheckOutDate(request.getCheckOut());
        booking.setStatus(BookingStatus.CREATED);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setNumberOfGuests(request.getNumberOfGuests());
        booking.setAdults(request.getAdults());
        booking.setChildren(request.getChildren());

        double totalAmount = 0.0;
        List<BookingRoom> bookingRooms = new ArrayList<>();

        for (Room room : rooms) {

            boolean isBooked = bookingRoomRepository.existsOverlappingBooking(
                    room.getId(),
                    request.getCheckIn(),
                    request.getCheckOut());

            if (isBooked) {
                throw new RuntimeException("Room not available: " + room.getId());
            }

            double roomPriceForStay = room.getPricePerNight() * days; // total for period
            totalAmount += roomPriceForStay;

            BookingRoom bookingRoom = new BookingRoom();
            bookingRoom.setBooking(booking);
            bookingRoom.setRoom(room);
            bookingRoom.setRoomPrice(roomPriceForStay);

            bookingRooms.add(bookingRoom);
        }

        booking.setAmount(totalAmount);
        booking.setBookingRooms(bookingRooms);

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    private BookingResponse mapToResponse(Booking booking) {
        List<BookingResponse.RoomSummary> rooms = booking.getBookingRooms()
                .stream()
                .map(br -> new BookingResponse.RoomSummary(
                        br.getRoom().getId(),
                        br.getRoomPrice()))
                .toList();

        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getId());
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setTotalAmount(booking.getAmount());
        response.setStatus(booking.getStatus().name());
        response.setRooms(rooms);

        return response;
    }

    @Transactional
    public ModifyBookingResponse modifyBooking(Long bookingId, ModifyBookingRequest request) {

    Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found for the provided ID."));

    if (booking.getStatus() == BookingStatus.CANCELLED)
        throw new RuntimeException("Cancelled booking cannot be modified");

    if (!booking.getCheckInDate().isAfter(LocalDate.now()))
        throw new RuntimeException("Past bookings cannot be modified");

    // Validate dates
    if (!request.getCheckIn().isAfter(LocalDate.now()))
        throw new RuntimeException("Check-in must be future date");

    if (!request.getCheckOut().isAfter(request.getCheckIn()))
        throw new RuntimeException("Invalid date range");

    // // Fetch rooms
    // List<Room> newRooms = roomRepository.findAllById(request.getRoomIds());
    // if (newRooms.size() != request.getRoomIds().size())
    //     throw new RuntimeException("Some rooms not found");

    List<Room> newRooms;

    if (request.getRoomIds() == null || request.getRoomIds().isEmpty()) {

        newRooms = booking.getBookingRooms()
                .stream()
                .map(BookingRoom::getRoom)
                .toList();

        if (newRooms.isEmpty()) {
            throw new RuntimeException("No rooms found in existing booking");
        }

    } else {

        newRooms = roomRepository.findAllById(request.getRoomIds());

        if (newRooms.size() != request.getRoomIds().size()) {
            throw new RuntimeException("Some rooms not found");
        }
    }

    int totalGuests = request.getAdults() + request.getChildren();

    int totalCapacity = newRooms.stream()
            .mapToInt(Room::getMaxGuest)
            .sum();

    if (totalGuests > totalCapacity)
        throw new RuntimeException("Selected rooms cannot accommodate guests");

    // Check availability (ignore same booking)
    for (int i = 0; i < newRooms.size(); i++) {
        Room room = newRooms.get(i);
        boolean isBooked = bookingRoomRepository.existsOverlappingBookingForModify(
                room.getId(),
                booking.getId(),
                request.getCheckIn(),
                request.getCheckOut());

        if (isBooked) {
            // Edge Case Fallback: The current room is taken. Let's try to find an IDENTICAL room
            // that is available for these dates so the customer doesn't just fail.
            List<Room> alternativeRooms = roomRepository.findByRoomTypeAndStatus(room.getRoomType(), com.example.demo.enums.RoomStatus.AVAILABLE);
            boolean foundAlternative = false;

            for (Room altRoom : alternativeRooms) {
                // don't check the one we just checked
                if (altRoom.getId().equals(room.getId())) continue;

                boolean altIsBooked = bookingRoomRepository.existsOverlappingBookingForModify(
                        altRoom.getId(),
                        booking.getId(),
                        request.getCheckIn(),
                        request.getCheckOut());

                if (!altIsBooked) {
                    newRooms.set(i, altRoom);
                    foundAlternative = true;
                    break;
                }
            }

            if (!foundAlternative) {
                throw new RuntimeException("Room not available and no alternative " + room.getRoomType() + " rooms free for these dates.");
            }
        }
    }

    long days = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());

    double newAmount = newRooms.stream()
            .mapToDouble(room -> room.getPricePerNight() * days)
            .sum();

    // Get last successful payment
    Payment lastPayment = paymentRepository
            .findTopByBookingIdAndStatusOrderByIdDesc(bookingId, PaymentStatus.SUCCESS)
            .orElseThrow(() -> new RuntimeException("Previous payment not found"));

    double oldAmount = lastPayment.getAmount();
    double difference = newAmount - oldAmount;

    // Rebuild booking rooms
    booking.getBookingRooms().clear();
    for (Room room : newRooms) {
        BookingRoom br = new BookingRoom();
        br.setBooking(booking);
        br.setRoom(room);
        br.setRoomPrice(room.getPricePerNight() * days);
        booking.getBookingRooms().add(br);
    }

    booking.setCheckInDate(request.getCheckIn());
    booking.setCheckOutDate(request.getCheckOut());
    booking.setNumberOfGuests(totalGuests);
    booking.setAmount(newAmount);

    // CASE 1: No price change (safe double comparison)
    if (Math.abs(difference) < 0.01) {
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        return new ModifyBookingResponse("Booking modified successfully", 0.0, null);
    }

    // Keep booking pending until financial adjustment completes
    booking.setStatus(BookingStatus.PENDING);
    bookingRepository.save(booking);

    // CASE 2: Extra payment required
    if (difference > 0) {

        Payment newPayment = new Payment();
        newPayment.setBooking(booking);
        newPayment.setAmount(difference); // Only extra amount
        newPayment.setRefundAmount(0.0);
        newPayment.setModified(true);
        newPayment.setStatus(PaymentStatus.INITIATED);
        newPayment.setTransactionId(UUID.randomUUID().toString());

        paymentRepository.save(newPayment);

        return new ModifyBookingResponse(
                "Additional payment required",
                difference,
                newPayment.getId()
        );
    }

    // CASE 3: Refund required
    double refundAmount = Math.abs(difference);

    Payment refundPayment = new Payment();
    refundPayment.setBooking(booking);
    refundPayment.setAmount(refundAmount); // Refund transaction amount
    refundPayment.setRefundAmount(refundAmount);
    refundPayment.setModified(true);
    refundPayment.setStatus(PaymentStatus.SUCCESS);
    refundPayment.setTransactionId(UUID.randomUUID().toString());
    refundPayment.setPaidAt(LocalDateTime.now());

    paymentRepository.save(refundPayment);

    booking.setStatus(BookingStatus.CONFIRMED);
    bookingRepository.save(booking);

    return new ModifyBookingResponse(
            "Refund processed",
            refundAmount,
            refundPayment.getId()
    );
}

    @Transactional
    public String cancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found for the provided ID."));

        // Already cancelled
        if (BookingStatus.CANCELLED.equals(booking.getStatus())) {
            throw new RuntimeException("Booking already cancelled");
        }

        // Checked in or checked out cannot be cancelled
        if (BookingStatus.CHECKED_IN.equals(booking.getStatus())
                || BookingStatus.CHECKED_OUT.equals(booking.getStatus())) {
            throw new RuntimeException("Checked-in bookings cannot be cancelled");
        }

        // Find last successful payment (if any)
        Payment payment = paymentRepository
                .findTopByBookingIdAndStatusOrderByIdDesc(bookingId, PaymentStatus.SUCCESS)
                .orElse(null);

        if (payment != null) {
            // Refund scenario (full)
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundAmount(payment.getAmount());
            paymentRepository.save(payment);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        return payment != null
                ? "Booking cancelled. Refund processed."
                : "Booking cancelled successfully.";
    }

    public PaginatedResponse<AdminBookingRow> list(
            String query,
            String status,
            LocalDate from,
            LocalDate to,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        var spec = Specification.where(BookingSpecifications.queryMatches(query))
                .and(BookingSpecifications.statusEquals(status))
                .and(BookingSpecifications.checkInBetween(from, to));

        Sort sort = Sort.by(
                ("asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC),
                (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy);

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);

        Page<Booking> result = bookingRepository.findAll(spec, pageable);

        List<AdminBookingRow> rows = result.getContent().stream().map(this::toRow).toList();

        return new PaginatedResponse<>(
                rows,
                result.getNumber() + 1, // 1-based for UI
                result.getSize(),
                result.getTotalElements());
    }

    private AdminBookingRow toRow(Booking b) {
        AdminBookingRow row = new AdminBookingRow();
        row.setId(b.getId());
        row.setUserId(b.getUser() != null ? b.getUser().getId() : null);
        row.setCheckInDate(b.getCheckInDate());
        row.setCheckOutDate(b.getCheckOutDate());
        row.setNumberOfGuests(b.getNumberOfGuests());
        row.setTotalAmount(b.getAmount());
        row.setStatus(b.getStatus() != null ? b.getStatus().name() : null);
        row.setCreatedAt(b.getCreatedAt());

        BookingRoom first = (b.getBookingRooms() != null && !b.getBookingRooms().isEmpty())
                ? b.getBookingRooms().get(0)
                : null;
        if (first != null && first.getRoom() != null) {
            row.setRoomId(first.getRoom().getId());
            row.setRoomName(first.getRoom().getRoomType() != null
                    ? first.getRoom().getRoomType()
                    : ("Room-" + first.getRoom().getId()));
        }

        if (b.getUser() != null) {
            row.setCustomerName(b.getUser().getUserName());
        }

        return row;
    }

    @Transactional
    public StatusUpdateResponse setStatus(Long bookingId, StatusUpdateRequest req) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found for the provided ID."));

        String target = req.getStatus();
        if (target == null)
            throw new RuntimeException("Status is required");

        BookingStatus newStatus;
        try {
            newStatus = BookingStatus.valueOf(target.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid status: " + target);
        }

        // Business rules to protect transitions
        if (booking.getStatus() == BookingStatus.CANCELLED && newStatus == BookingStatus.CONFIRMED) {
            throw new RuntimeException("Cannot confirm a cancelled booking");
        }
        if (booking.getStatus() == BookingStatus.CHECKED_IN && newStatus == BookingStatus.CANCELLED) {
            throw new RuntimeException("Checked-in booking cannot be cancelled");
        }

        booking.setStatus(newStatus);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        return new StatusUpdateResponse(booking.getId(), booking.getStatus().name(), booking.getUpdatedAt());
    }

    public AdminBookingRow getOne(Long bookingId) {
        var b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found for the provided ID."));
        return toRow(b);
    }
}


