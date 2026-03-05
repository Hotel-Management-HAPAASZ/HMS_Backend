package com.example.demo.dto;

import com.example.demo.enums.ComplaintCategory;
import com.example.demo.enums.ComplaintStatus;
import com.example.demo.enums.ContactPreference;
import com.example.demo.enums.PriorityOfComplaint;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintResponse {
    private Long id;              // DB primary key — use for admin API calls
    private String referenceNumber;
    private Long bookingId;
    private String title;
    private String description;
    private ComplaintCategory category;
    private ContactPreference contactPreference;
    private ComplaintStatus status;
    private PriorityOfComplaint priority;
    private Long assignedUserId;       // User.id of the assigned staff member
    private String assignedUserName;   // display name for the assignee
    private LocalDateTime expectedResolutionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}