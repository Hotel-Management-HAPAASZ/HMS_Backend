package com.example.demo.services;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.demo.models.User;
import com.example.demo.enums.ComplaintStatus;
import com.example.demo.models.Complaint;
import com.example.demo.models.ComplaintAction;
import com.example.demo.models.Staff;
import com.example.demo.models.Booking;
import com.example.demo.dto.ComplaintRequest;
import com.example.demo.dto.ComplaintUpdateRequest;
import com.example.demo.dto.ComplaintResponse;
import com.example.demo.dto.ComplaintActionRequest;
import com.example.demo.globalException.ComplaintNotFoundException;
import com.example.demo.globalException.InvalidComplaintStateException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.repository.ComplaintActionRepository;
import com.example.demo.repository.ComplaintRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.StaffRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintActionRepository complaintActionRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final BookingRepository bookingRepository;
    private final ReferenceGeneratorService referenceGeneratorService;

    @Transactional
    public ComplaintResponse createComplaint(ComplaintRequest request, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("user not found"));

        final java.time.LocalDate today = java.time.LocalDate.now();

        // ✅ Enforce: complaints can only be raised for an active stay (guest currently at hotel)
        // - If bookingId provided: validate ownership + active date window
        // - If bookingId missing: auto-pick the current active booking for the user (or reject)
        Booking booking;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found for the provided ID. Please verify your booking reference."));

            if (booking.getUser() == null || booking.getUser().getId() == null || !booking.getUser().getId().equals(userId)) {
                throw new RuntimeException("You can only raise complaints for your own booking.");
            }

            // Must be currently staying: checkIn <= today <= checkOut
            if (booking.getCheckInDate() == null || booking.getCheckOutDate() == null
                    || today.isBefore(booking.getCheckInDate())
                    || today.isAfter(booking.getCheckOutDate())) {
                throw new RuntimeException("You can only raise complaints during your stay (between check-in and check-out dates).");
            }

            // Must be actually checked-in
            if (booking.getStatus() == null || booking.getStatus() != com.example.demo.enums.BookingStatus.CHECKED_IN) {
                throw new RuntimeException("You can only raise complaints after check-in.");
            }
        } else {
            booking = bookingRepository.findActiveStayBookingForUser(userId, today)
                .orElseThrow(() -> new RuntimeException("You can only raise a complaint during an active stay. No current booking found."));
        }

        Complaint complaint = Complaint.builder()
            .referenceNumber(referenceGeneratorService.generateComplaintReference())
            .user(user)
            .booking(booking)
            .category(request.getCategory())
            .title(request.getTitle())
            .description(request.getDescription())
            .contactPreference(request.getContactPreference())
            .priority(request.getPriority())
            .status(ComplaintStatus.OPEN)
            .expectedResolutionDate(LocalDateTime.now().plusHours(48)) // SLA Example
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        Complaint saved = complaintRepository.save(complaint);
        return toResponse(saved);
    }

    @Transactional
    public ComplaintResponse updateComplaint(String reference, ComplaintUpdateRequest request, Long userId) {
        Complaint complaint = getByReferenceAndUser(reference, userId);

        if (complaint.getStatus() != ComplaintStatus.OPEN) {
            throw new InvalidComplaintStateException("Complaint can only be edited when in OPEN status.");
        }

        if (request.getTitle() != null) complaint.setTitle(request.getTitle());
        if (request.getDescription() != null) complaint.setDescription(request.getDescription());
        if (request.getContactPreference() != null) complaint.setContactPreference(request.getContactPreference());

        complaint.setUpdatedAt(LocalDateTime.now());

        return toResponse(complaintRepository.save(complaint));
    }

    @Transactional
    public void resolveComplaint(String reference) {
        Complaint complaint = getComplaintByReference(reference);
        if (complaint.getStatus() == ComplaintStatus.CLOSED) {
            throw new InvalidComplaintStateException("Cannot resolve a CLOSED complaint.");
        }
        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);
    }

    @Transactional
    public void confirmResolution(String reference, Long userId) {
        Complaint complaint = getByReferenceAndUser(reference, userId);
        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new InvalidComplaintStateException("Only RESOLVED complaints can be confirmed by customer.");
        }
        complaint.setStatus(ComplaintStatus.CLOSED);
        complaint.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);
    }

    @Transactional
    public void reopenComplaint(String reference, Long userId) {
        Complaint complaint = getByReferenceAndUser(reference, userId);
        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new InvalidComplaintStateException("Only RESOLVED complaints can be reopened by customer.");
        }
        complaint.setStatus(ComplaintStatus.OPEN);
        complaint.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);
    }

    public Page<ComplaintResponse> myComplaints(Long userId, ComplaintStatus status, Pageable pageable) {
        Page<Complaint> page = (status == null)
            ? complaintRepository.findByUser_Id(userId, pageable)
            : complaintRepository.findByUser_IdAndStatus(userId, status, pageable);

        return page.map(this::toResponse);
    }

    public ComplaintResponse track(String reference, Long userId) {
        return toResponse(getByReferenceAndUser(reference, userId));
    }

    public Page<ComplaintResponse> getAllComplaints(ComplaintStatus status, Pageable pageable) {
        Page<Complaint> page = (status == null)
            ? complaintRepository.findAll(pageable)
            : complaintRepository.findByStatus(status, pageable);

        return page.map(this::toResponse);
    }

    @Transactional
    public ComplaintResponse addComplaintAction(ComplaintActionRequest request) {
        Complaint complaint = complaintRepository.findById(request.getComplaintId())
            .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

        Staff staff = staffRepository.findByUserId(request.getStaffId())
            .orElseThrow(() -> new RuntimeException("Staff not found for user id: " + request.getStaffId()));

        ComplaintAction action = new ComplaintAction();
        action.setComplaint(complaint);
        action.setStaff(staff);
        action.setActionNote(request.getActionNote());
        action.setStatus(request.getStatus());
        action.setActionAt(LocalDateTime.now());
        complaintActionRepository.save(action);

        complaint.setStatus(request.getStatus());
        complaint.setAssignedStaff(staff);
        complaint.setUpdatedAt(LocalDateTime.now());
        Complaint updated = complaintRepository.save(complaint);
        return toResponse(updated);
    }

    private Complaint getComplaintByReference(String reference) {
        return complaintRepository.findByReferenceNumber(reference)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found: " + reference));
    }

    private Complaint getByReferenceAndUser(String reference, Long userId) {
        Complaint complaint = getComplaintByReference(reference);
        if (complaint.getUser() == null || !complaint.getUser().getId().equals(userId)) {
            throw new ComplaintNotFoundException("Complaint not found or not owned by user");
        }
        return complaint;
    }

    private ComplaintResponse toResponse(Complaint c) {
        // Get the latest staff action note (resolution note)
        String latestNote = complaintActionRepository
            .findTopByComplaintIdOrderByActionAtDesc(c.getId())
            .map(a -> a.getActionNote())
            .orElse(null);

        return ComplaintResponse.builder()
            .id(c.getId())
            .referenceNumber(c.getReferenceNumber())
            .bookingId(c.getBooking() != null ? c.getBooking().getId() : null)
            .title(c.getTitle())
            .description(c.getDescription())
            .status(c.getStatus())
            .category(c.getCategory())
            .contactPreference(c.getContactPreference())
            .priority(c.getPriority())
            .assignedUserId(c.getAssignedStaff() != null && c.getAssignedStaff().getUser() != null ? c.getAssignedStaff().getUser().getId() : null)
            .assignedUserName(c.getAssignedStaff() != null && c.getAssignedStaff().getUser() != null ? c.getAssignedStaff().getUser().getUserName() : null)
            .expectedResolutionDate(c.getExpectedResolutionDate())
            .createdAt(c.getCreatedAt())
            .updatedAt(c.getUpdatedAt())
            .resolutionNote(latestNote)
            .build();
    }
}
