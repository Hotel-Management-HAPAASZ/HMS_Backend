package com.example.demo.globalException;

public class InvalidComplaintStateException extends RuntimeException {
    public InvalidComplaintStateException(String message) {
        super(message);
    }
}
