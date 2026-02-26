// src/main/java/com/example/demo/controller/ComplaintController.java
package com.example.demo.controller;

import com.example.demo.dto.ComplaintRequest;
import com.example.demo.dto.ComplaintResponse;
import com.example.demo.enums.ComplaintStatus;
import com.example.demo.services.ComplaintService;
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
    public ResponseEntity<ComplaintResponse> createComplaints(
        @RequestBody ComplaintRequest complaintRequest,
        @PathVariable Long userId
    ) {
        ComplaintResponse complaint = complaintService.createComplaint(complaintRequest, userId);
        return ResponseEntity.ok(complaint);
    }

    // 🔎 Paged list for current user
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

    // 🎫 Track by public id
    @GetMapping("/track/{complaintId}")
    public ResponseEntity<ComplaintResponse> track(
        @PathVariable String complaintId,
        @RequestParam Long userId
    ) {
        return ResponseEntity.ok(complaintService.track(complaintId, userId));
    }

    // 📋 Paged list for all complaints (Admin/Staff)
    @GetMapping("/all")
    public ResponseEntity<Page<ComplaintResponse>> allComplaints(
        @RequestParam(required = false) ComplaintStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(complaintService.getAllComplaints(status, pageable));
    }

    // ✍️ Staff updates a complaint status
    @PostMapping("/action")
    public ResponseEntity<ComplaintResponse> addAction(
        @RequestBody com.example.demo.dto.ComplaintActionRequest actionRequest
    ) {
        ComplaintResponse updated = complaintService.addComplaintAction(actionRequest);
        return ResponseEntity.ok(updated);
    }
}