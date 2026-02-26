
package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dto.PaymentStartRequest;
import com.example.demo.dto.PaymentResponse;
import com.example.demo.dto.PaymentVerifyRequest;
import com.example.demo.services.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService){
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@RequestBody PaymentStartRequest request){
        PaymentResponse response = paymentService.initiatePayment(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("verify")
    public ResponseEntity<PaymentResponse> verifyPayment(@RequestBody PaymentVerifyRequest request){

        PaymentResponse response = paymentService.verifyPayment(request);

        return ResponseEntity.ok(response);
    }
}
