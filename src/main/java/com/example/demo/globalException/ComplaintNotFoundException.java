package com.example.demo.globalException;

public class ComplaintNotFoundException extends RuntimeException {
    public ComplaintNotFoundException(String message) {
        super(message);
    }
}
