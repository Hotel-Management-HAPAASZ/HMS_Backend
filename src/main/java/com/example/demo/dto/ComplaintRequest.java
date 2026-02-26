package com.example.demo.dto;

import com.example.demo.enums.ComplaintCategory;

import com.example.demo.enums.PriorityOfComplaint;
import lombok.Data;

@Data
public class ComplaintRequest{

    private Long bookingId;

    private ComplaintCategory category;

    private String title;

    private String description;

    private PriorityOfComplaint priority;
}