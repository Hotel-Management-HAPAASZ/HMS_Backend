package com.example.demo.controller;
import java.util.*;


import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.*;
import com.example.demo.enums.*;
import com.example.demo.services.*;
import com.example.demo.models.*;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import lombok.*;




@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController{
    
    private final StaffService staffService;
    // private final StaffComplaintService staffComplaintService;

    @GetMapping("/get-assigned-complaints")
    public ResponseEntity<List<ComplaintResponse>> getAssignedComplaints(
        @RequestParam Long staffId
    )
    {
       List<ComplaintResponse> complaints= staffService.getAssignedComplaints(staffId);
        return ResponseEntity.ok(complaints);
    }

    @PutMapping("/update-complaint-status/{id}")
    public ResponseEntity<ComplaintResponse> updateComplaintStatus(
        @PathVariable Long id,
        @RequestParam ComplaintStatus status,
        @RequestParam Long staffId

    )
    {
       
       ComplaintResponse updatedComplaint = staffService.updateComplaintStatus(id,status,staffId);
        return ResponseEntity.ok(updatedComplaint);
    }

    @PostMapping("/add-action")

    public ResponseEntity<String> addAction(@RequestBody ComplaintActionRequest request) {
    staffService.addComplaintAction(request);
    return ResponseEntity.ok("Action logged successfully");
  }

}