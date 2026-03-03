package com.example.demo.controller;

import com.example.demo.dto.ComplaintRequest;
import com.example.demo.dto.ComplaintUpdateRequest;
import com.example.demo.dto.ComplaintResponse;
import com.example.demo.enums.ComplaintStatus;
import com.example.demo.services.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaint")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping("/create/{userId}")
    public ResponseEntity<ComplaintResponse> createComplaint(
        @Valid @RequestBody ComplaintRequest complaintRequest,
        @PathVariable Long userId
    ) {
        ComplaintResponse complaint = complaintService.createComplaint(complaintRequest, userId);
        return ResponseEntity.ok(complaint);
    }

    @PutMapping("/update/{reference}")
    public ResponseEntity<ComplaintResponse> updateComplaint(
        @PathVariable String reference,
        @Valid @RequestBody ComplaintUpdateRequest updateRequest,
        @RequestParam Long userId
    ) {
        ComplaintResponse updated = complaintService.updateComplaint(reference, updateRequest, userId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{reference}/resolve")
    public ResponseEntity<Void> resolveComplaint(@PathVariable String reference) {
        // Typically Staff/Admin
        complaintService.resolveComplaint(reference);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{reference}/confirm")
    public ResponseEntity<Void> confirmResolution(
        @PathVariable String reference,
        @RequestParam Long userId
    ) {
        complaintService.confirmResolution(reference, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{reference}/reopen")
    public ResponseEntity<Void> reopenComplaint(
        @PathVariable String reference,
        @RequestParam Long userId
    ) {
        complaintService.reopenComplaint(reference, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ComplaintResponse>> myComplaints(
        @RequestParam Long userId,
        @RequestParam(required = false) ComplaintStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(complaintService.myComplaints(userId, status, pageable));
    }

    @GetMapping("/track/{reference}")
    public ResponseEntity<ComplaintResponse> track(
        @PathVariable String reference,
        @RequestParam Long userId
    ) {
        return ResponseEntity.ok(complaintService.track(reference, userId));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ComplaintResponse>> allComplaints(
        @RequestParam(required = false) ComplaintStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(complaintService.getAllComplaints(status, pageable));
    }

    @PostMapping("/action")
    public ResponseEntity<ComplaintResponse> addAction(
        @RequestBody com.example.demo.dto.ComplaintActionRequest actionRequest
    ) {
        ComplaintResponse updated = complaintService.addComplaintAction(actionRequest);
        return ResponseEntity.ok(updated);
    }
}