package com.example.demo.controller;


import com.example.demo.dto.InvoiceResponse;
import com.example.demo.services.InvoiceService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<InvoiceResponse> getInvoiceByBookingId(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                invoiceService.getInvoiceByBookingId(bookingId)
        );
    }
}