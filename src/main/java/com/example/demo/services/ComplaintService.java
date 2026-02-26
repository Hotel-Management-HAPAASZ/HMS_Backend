package com.example.demo.services;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.models.User;
import com.example.demo.enums.*;
import com.example.demo.enums.ComplaintStatus;
import com.example.demo.models.Complaint;
import com.example.demo.models.ComplaintAction;
import com.example.demo.models.Staff;
import com.example.demo.models.Booking;
import com.example.demo.dto.*;

import org.springframework.stereotype.Service;
import com.example.demo.repository.ComplaintActionRepository;
import com.example.demo.repository.ComplaintRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.StaffRepository;
import lombok.*;
import java.util.*;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintActionRepository complaintActionRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final BookingRepository bookingRepository;

    ComplaintService(ComplaintRepository complaintRepository,ComplaintActionRepository complaintActionRepository,UserRepository userRepository,StaffRepository staffRepository,BookingRepository bookingRepository){
        this.complaintRepository = complaintRepository;
        this.complaintActionRepository=complaintActionRepository;
        this.userRepository=userRepository;
        this.staffRepository=staffRepository;
        this.bookingRepository=bookingRepository;
    }


public ComplaintResponse createComplaint(ComplaintRequest complaintRequest, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("user not found"));
        Booking booking = bookingRepository.findById(complaintRequest.getBookingId())
            .orElseThrow(() -> new RuntimeException("booking not found"));

        String humanReadableId = generateComplaintId();

        Complaint c = new Complaint();
        c.setComplaintId(humanReadableId);
        c.setUser(user);
        c.setBooking(booking);
        c.setCategory(complaintRequest.getCategory());
        c.setDescription(complaintRequest.getDescription());
        c.setStatus(ComplaintStatus.OPEN);
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        c.setTitle(complaintRequest.getTitle());
        c.setExpectedResolutionDate(LocalDateTime.now().plusDays(3));
        c.setPriority(complaintRequest.getPriority());

        Complaint saved = complaintRepository.save(c);

        // Return a normalized response the UI understands
        return toResponse(saved);
    }

    public String generateComplaintId() {
        long count = complaintRepository.count();
        long nextNumber = count + 1;
        return String.format("CMP-%03d", nextNumber);
    }

    // ---------- NEW: list complaints of a user (paged) ----------
    public Page<ComplaintResponse> myComplaints(Long userId, ComplaintStatus status, Pageable pageable) {
        Page<Complaint> page = (status == null)
            ? complaintRepository.findByUser_Id(userId, pageable)
            : complaintRepository.findByUser_IdAndStatus(userId, status, pageable);

        return page.map(this::toResponse);
    }

    // ---------- NEW: track complaint by public complaintId for a user ----------
    public ComplaintResponse track(String complaintId, Long userId) {
        Complaint c = complaintRepository.findByComplaintId(complaintId)
            .filter(it -> it.getUser() != null && it.getUser().getId().equals(userId))
            .orElseThrow(() -> new RuntimeException("Complaint not found or not owned by user"));
        return toResponse(c);
    }

    // ---------- NEW: list all complaints (Admin/Staff) ----------
    public Page<ComplaintResponse> getAllComplaints(ComplaintStatus status, Pageable pageable) {
        Page<Complaint> page = (status == null)
            ? complaintRepository.findAll(pageable)
            : complaintRepository.findByStatus(status, pageable);

        return page.map(this::toResponse);
    }

    // ---------- NEW: record a staff action on a complaint ----------
    public ComplaintResponse addComplaintAction(ComplaintActionRequest request) {
        Complaint complaint = complaintRepository.findById(request.getComplaintId())
            .orElseThrow(() -> new RuntimeException("Complaint not found"));

        Staff staff = staffRepository.findById(request.getStaffId())
            .orElseThrow(() -> new RuntimeException("Staff not found"));

        ComplaintAction action = new ComplaintAction();
        action.setComplaint(complaint);
        action.setStaff(staff);
        action.setActionNote(request.getActionNote());
        action.setStatus(request.getStatus());
        action.setActionAt(LocalDateTime.now());
        complaintActionRepository.save(action);

        // Update the main complaint status/updatedAt/assignedStaff
        complaint.setStatus(request.getStatus());
        complaint.setUpdatedAt(LocalDateTime.now());
        complaint.setAssignedStaff(staff);

        Complaint updated = complaintRepository.save(complaint);
        return toResponse(updated);
    }

    // ---------- Mapper ----------
    private ComplaintResponse toResponse(Complaint c) {
        return ComplaintResponse.builder()
            .id(c.getId())
            .complaintId(c.getComplaintId())
            .title(c.getTitle())
            .subject(c.getTitle()) // UI reads 'subject' if present
            .description(c.getDescription())
            .status(c.getStatus())
            .category(c.getCategory())
            .priority(c.getPriority())
            .createdAt(c.getCreatedAt())
            .updatedAt(c.getUpdatedAt())
            .build();
    }

}
