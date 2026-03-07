package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.*;

// import com.example.demo.models.Booking;

import com.example.demo.services.BookingService;
import com.example.demo.services.UserBookingHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserBookingHistoryService userBookingHistoryService;

    @PostMapping("/check")
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {

        BookingResponse response = bookingService.createBooking(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<UserBookingHistoryResponse>> getUserBookingHistory(@PathVariable Long userId) {
        List<UserBookingHistoryResponse> bookings = userBookingHistoryService.getUserBookings(userId);

        return ResponseEntity.ok(bookings);
    }

    // Active (checked-in) bookings for complaint dropdown
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<UserBookingHistoryResponse>> getActiveBookings(@PathVariable Long userId) {
        List<UserBookingHistoryResponse> active = bookingService.getActiveBookingsForUser(userId);
        return ResponseEntity.ok(active);
    }

    @GetMapping("/admin/history")
    public ResponseEntity<List<UserBookingHistoryResponse>> getAllUserBookingHistory() {
        List<UserBookingHistoryResponse> bookings = userBookingHistoryService.getAllBookings();

        return ResponseEntity.ok(bookings);
    }

    // MODIFY booking by user
    @PatchMapping("/{bookingId}/modify")
    public ResponseEntity<ModifyBookingResponse> modifyBooking(@PathVariable Long bookingId,
            @RequestBody ModifyBookingRequest request) {

        ModifyBookingResponse response = bookingService.modifyBooking(bookingId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{bookingId}/cancel")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) {

        String response = bookingService.cancelBooking(bookingId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<AdminBookingRow>> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page, // 0-based for backend
            @RequestParam(name = "pageSize", defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        var result = bookingService.list(query, status, from, to, page, size, sortBy, sortDir); // 👈 instance
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<AdminBookingRow> getOne(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getOne(bookingId)); // 👈 instance
    }

    @PatchMapping("/{bookingId}/status")
    public ResponseEntity<StatusUpdateResponse> setStatus(
            @PathVariable Long bookingId,
            @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(bookingService.setStatus(bookingId, req)); // 👈 instance
    }

}
