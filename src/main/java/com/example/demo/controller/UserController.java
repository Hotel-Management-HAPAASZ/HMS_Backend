package com.example.demo.controller;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.CreateUserWithStaffRequest;
import com.example.demo.dto.InvoiceResponse;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.enums.AccountStatus;
import com.example.demo.enums.UserRole;
import com.example.demo.models.User;
import com.example.demo.repository.RoomRepository;
import com.example.demo.services.AdminService;
import com.example.demo.services.InvoiceService;
import com.example.demo.services.UserService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AdminService adminService;
    private final RoomRepository roomRepository;
    private final UserService userService;

  

      
@GetMapping("/users")
public Page<User> getAllUsers(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UserRole role,
        @RequestParam(required = false) AccountStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "userName,asc") String sort
) {
    return adminService.getAllUsers(search, role, status, page, size, sort);
}


    @PostMapping("/users")
    public ResponseEntity<?> createUser(
            @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(adminService.createUser(request));
    }

     @PostMapping("/staff")
    public ResponseEntity<?> createStaff(
            @RequestBody CreateUserWithStaffRequest request) {

           CreateUserRequest userRequest = request.getUser();
           String department = request.getDepartmentName();
        return ResponseEntity.ok(adminService.createStaff(userRequest,department));
        
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(adminService.updateUser(userId, request));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam boolean active) {
        adminService.updateUserStatus(userId, active);
        return ResponseEntity.ok("User status updated");
    }
    
    //deactive

   @PatchMapping("users/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable Long id) {
        String message = adminService.deactivateUser(id);
        return ResponseEntity.ok(Map.of("message", message, "status", "INACTIVE"));
    }


    // Reactivate
    @PatchMapping("users/{id}/reactivate")
    public ResponseEntity<Map<String, String>> reactivate(@PathVariable Long id) {
        String message = adminService.reactivateUser(id);
        return ResponseEntity.ok(Map.of("message", message, "status", "ACTIVE"));
    }

    // Optional: Hard delete — protect with roles and confirmation
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> hardDelete(@PathVariable Long id) {
        String message = adminService.hardDeleteUser(id);
        return ResponseEntity.ok(Map.of("message", message));
    }

}
