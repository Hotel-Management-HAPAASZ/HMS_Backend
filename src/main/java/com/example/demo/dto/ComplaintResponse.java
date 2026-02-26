// src/main/java/com/example/demo/dto/ComplaintResponse.java
package com.example.demo.dto;

import com.example.demo.enums.ComplaintCategory;
import com.example.demo.enums.ComplaintStatus;
import com.example.demo.enums.PriorityOfComplaint;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintResponse {
    private Long id;
    private String complaintId;
    private String title;
    private String subject;           // duplicate of title for UI convenience
    private String description;
    private ComplaintStatus status;
    private ComplaintCategory category;
    private PriorityOfComplaint priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    
    private LocalDateTime submissionDate;
    private LocalDateTime expectedResolutionDate;
}