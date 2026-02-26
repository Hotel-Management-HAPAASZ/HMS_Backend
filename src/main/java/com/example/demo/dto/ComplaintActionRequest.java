package com.example.demo.dto;

import com.example.demo.enums.ComplaintCategory;

import com.example.demo.enums.ComplaintStatus;
import lombok.Data;

@Data
public class ComplaintActionRequest{

    private Long complaintId;

    private Long staffId;

    private String actionNote;

    private ComplaintStatus status;

}
