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

        Booking booking = null;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("booking not found"));

            // Check if customer is actually at the hotel or check-in date has passed
            if (booking.getCheckInDate().isAfter(java.time.LocalDate.now())) {
                throw new RuntimeException("Cannot raise a complaint for a booking before the check-in date.");
            }
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

        return toResponse(complaintRepository.save(complaint));
    }

    @Transactional
    public void resolveComplaint(String reference) {
        Complaint complaint = getComplaintByReference(reference);
        if (complaint.getStatus() == ComplaintStatus.CLOSED) {
            throw new InvalidComplaintStateException("Cannot resolve a CLOSED complaint.");
        }
        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaintRepository.save(complaint);
    }

    @Transactional
    public void confirmResolution(String reference, Long userId) {
        Complaint complaint = getByReferenceAndUser(reference, userId);
        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new InvalidComplaintStateException("Only RESOLVED complaints can be confirmed by customer.");
        }
        complaint.setStatus(ComplaintStatus.CLOSED);
        complaintRepository.save(complaint);
    }

    @Transactional
    public void reopenComplaint(String reference, Long userId) {
        Complaint complaint = getByReferenceAndUser(reference, userId);
        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new InvalidComplaintStateException("Only RESOLVED complaints can be reopened by customer.");
        }
        complaint.setStatus(ComplaintStatus.OPEN);
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

        Staff staff = staffRepository.findById(request.getStaffId())
            .orElseThrow(() -> new RuntimeException("Staff not found"));

        ComplaintAction action = new ComplaintAction();
        action.setComplaint(complaint);
        action.setStaff(staff);
        action.setActionNote(request.getActionNote());
        action.setStatus(request.getStatus());
        action.setActionAt(LocalDateTime.now());
        complaintActionRepository.save(action);

        complaint.setStatus(request.getStatus());
        complaint.setAssignedStaff(staff);
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
        return ComplaintResponse.builder()
            .referenceNumber(c.getReferenceNumber())
            .bookingId(c.getBooking() != null ? c.getBooking().getId() : null)
            .title(c.getTitle())
            .description(c.getDescription())
            .status(c.getStatus())
            .category(c.getCategory())
            .contactPreference(c.getContactPreference())
            .priority(c.getPriority())
            .expectedResolutionDate(c.getExpectedResolutionDate())
            .createdAt(c.getCreatedAt())
            .updatedAt(c.getUpdatedAt())
            .build();
    }
}
