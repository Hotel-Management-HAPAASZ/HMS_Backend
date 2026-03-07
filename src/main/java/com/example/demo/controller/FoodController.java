package com.example.demo.controller;

import com.example.demo.dto.food.*;
import com.example.demo.enums.FoodOrderStatus;
import com.example.demo.services.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    // Customer: view menu (authenticated because ordering is only for active stay anyway)
    @GetMapping("/menu")
    public ResponseEntity<List<FoodMenuItemResponse>> menu() {
        return ResponseEntity.ok(foodService.getMenu());
    }

    // Customer: get checked-in bookings (for room selection)
    @GetMapping("/checked-in-bookings")
    public ResponseEntity<List<CheckedInBookingResponse>> getCheckedInBookings(@RequestParam Long userId) {
        return ResponseEntity.ok(foodService.getCheckedInBookings(userId));
    }

    // Food staff/admin: create menu item
    @PostMapping("/menu")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FOOD_STAFF')")
    public ResponseEntity<FoodMenuItemResponse> createMenuItem(@RequestBody FoodMenuItemResponse payload) {
        return ResponseEntity.ok(foodService.upsertMenuItem(null, payload));
    }

    // Food staff/admin: update menu item
    @PutMapping("/menu/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FOOD_STAFF')")
    public ResponseEntity<FoodMenuItemResponse> updateMenuItem(@PathVariable Long id, @RequestBody FoodMenuItemResponse payload) {
        return ResponseEntity.ok(foodService.upsertMenuItem(id, payload));
    }

    // Food staff/admin: delete menu item
    @DeleteMapping("/menu/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FOOD_STAFF')")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        foodService.deleteMenuItem(id);
        return ResponseEntity.ok().build();
    }

    // Customer: place order + initiate "pay now" (CARD + OTP)
    @PostMapping("/orders")
    public ResponseEntity<FoodPaymentResponse> createOrder(@Valid @RequestBody CreateFoodOrderRequest request) {
        return ResponseEntity.ok(foodService.createOrderAndInitiatePayment(request));
    }

    // Customer: verify OTP to complete payment
    @PostMapping("/payments/verify")
    public ResponseEntity<FoodPaymentResponse> verifyFoodPayment(@Valid @RequestBody FoodPaymentVerifyRequest request) {
        return ResponseEntity.ok(foodService.verifyPayment(request));
    }

    // Customer: view their orders
    @GetMapping("/orders/my")
    public ResponseEntity<List<FoodOrderResponse>> myOrders(@RequestParam Long userId) {
        return ResponseEntity.ok(foodService.myOrders(userId));
    }

    // Food staff/admin: view pending orders
    @GetMapping("/orders/pending")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FOOD_STAFF')")
    public ResponseEntity<List<FoodOrderResponse>> pending() {
        return ResponseEntity.ok(foodService.pendingOrders());
    }

    // Food staff/admin: update order status
    @PatchMapping("/orders/{orderId}/status")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FOOD_STAFF')")
    public ResponseEntity<FoodOrderResponse> updateStatus(
            @PathVariable Long orderId,
            @RequestParam FoodOrderStatus status
    ) {
        return ResponseEntity.ok(foodService.updateOrderStatus(orderId, status));
    }
}


