package com.example.demo.dto;

import com.example.demo.enums.ContactPreference;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComplaintUpdateRequest {

    @Size(min = 10, max = 100, message = "Title must be between 10 and 100 characters")
    private String title;

    @Size(min = 20, max = 500, message = "Description must be between 20 and 500 characters")
    private String description;

    private ContactPreference contactPreference;
}
