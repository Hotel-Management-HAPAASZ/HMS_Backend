package com.example.demo.dto;

import com.example.demo.enums.ComplaintCategory;
import com.example.demo.enums.ContactPreference;
import com.example.demo.enums.PriorityOfComplaint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComplaintRequest {

    private Long bookingId; // Could be null if raised without a booking

    @NotNull(message = "Category is required")
    private ComplaintCategory category;

    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 100, message = "Title must be between 10 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 500, message = "Description must be between 20 and 500 characters")
    private String description;

    @NotNull(message = "Contact preference is required")
    private ContactPreference contactPreference;

    private PriorityOfComplaint priority; // Preserving existing field
}