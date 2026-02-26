package com.example.demo.controller;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dto.*;
import com.example.demo.dto.billDto.CreateBillRequest;
import com.example.demo.dto.billDto.UpdateBillRequest;
import com.example.demo.enums.AccountStatus;
import com.example.demo.enums.BookingStatus;
import com.example.demo.enums.ComplaintCategory;
import com.example.demo.enums.ComplaintStatus;
import com.example.demo.enums.RoomStatus;
import com.example.demo.enums.UserRole;
import com.example.demo.models.Room;
import com.example.demo.models.User;
import com.example.demo.repository.RoomRepository;
import com.example.demo.services.AdminService;
import com.example.demo.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final RoomRepository roomRepository;
private final UserService userService;
    /*
     * =====================================================
     * US014 – ADMIN DASHBOARD
     * =====================================================
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardData());
    }

   
    /*
     * =====================================================
     * US017 & US018 – RESERVATION MANAGEMENT
     * =====================================================
     */
    // @GetMapping("/bookings")
    // public ResponseEntity<?> getAllBookings() {
    //     return ResponseEntity.ok(adminService.getAllBookings());
    // }

    // @GetMapping("/bookings/search")
    // public ResponseEntity<?> searchBookings(
    //         @RequestParam(required = false) BookingStatus status,
    //         @RequestParam(required = false) LocalDate startDate,
    //         @RequestParam(required = false) LocalDate endDate) {
    //     return ResponseEntity.ok(
    //             adminService.searchBookings(status, startDate, endDate));
    // }

    // @PostMapping("/bookings")
    // public ResponseEntity<?> createBooking(
    //         @RequestBody AdminBookingRequest request) {
    //     return ResponseEntity.ok(adminService.createBooking(request));
    // }

    // @PutMapping("/bookings/{bookingId}")
    // public ResponseEntity<?> updateBooking(
    //         @PathVariable Long bookingId,
    //         @RequestBody AdminUpdateBookingRequest request) {
    //     return ResponseEntity.ok(
    //             adminService.updateBooking(bookingId, request));
    // }

    // @DeleteMapping("/bookings/{bookingId}")
    // public ResponseEntity<?> cancelBooking(
    //         @PathVariable Long bookingId) {
    //     adminService.cancelBooking(bookingId);
    //     return ResponseEntity.ok("Booking cancelled successfully");
    // }

    /*
     * =====================================================
     * US019 – USER MANAGEMENT
     * =====================================================
     */
 

    // @PostMapping("/users/{userId}/reset-password")
    // public ResponseEntity<?> resetPassword(@PathVariable Long userId) {
    // adminService.resetPassword(userId);
    // return ResponseEntity.ok("Password reset successfully");
    // }

    /*
     * =====================================================
     * US020 & US021 – BILL MANAGEMENT
     * =====================================================
     */
    @GetMapping("/bills")
    public ResponseEntity<?> getAllBills() {
        return ResponseEntity.ok(adminService.getAllBills());
    }

    @GetMapping("/bills/search")
    public ResponseEntity<?> searchBills(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(adminService.searchBills(userId));
    }

    @PostMapping("/bills")
    public ResponseEntity<?> createBill(
            @RequestBody CreateBillRequest request) {
        return ResponseEntity.ok(adminService.createBill(request));
    }

    @PutMapping("/bills/{billId}")
    public ResponseEntity<?> updateBill(
            @PathVariable Long billId,
            @RequestBody UpdateBillRequest request) {
        return ResponseEntity.ok(adminService.updateBill(billId, request));
    }

    /*
     * =====================================================
     * US022 – COMPLAINT MANAGEMENT
     * =====================================================
     */
    @GetMapping("/complaints/{category}/{status}")
    public ResponseEntity<?> getAllComplaints(
            @PathVariable ComplaintStatus status,
            @PathVariable ComplaintCategory category) {
        return ResponseEntity.ok(adminService.getAllComplaints(status, category));
    }

    @PutMapping("/complaints/{complaintId}/assign")
    public ResponseEntity<?> assignComplaint(
            @PathVariable Long complaintId,
            @RequestParam Long staffId) {
        adminService.assignComplaint(complaintId, staffId);
        return ResponseEntity.ok("Complaint assigned successfully");
    }

    @PutMapping("/complaints/{complaintId}/status")
    public ResponseEntity<?> updateComplaintStatus(
            @PathVariable Long complaintId,
            @RequestParam ComplaintStatus status) {
        adminService.updateComplaintStatus(complaintId, status);
        return ResponseEntity.ok("Complaint status updated");
    }
}