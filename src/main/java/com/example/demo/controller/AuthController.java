package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.enums.UserRole;
import com.example.demo.models.User;
import com.example.demo.security.jwt.JwtService;
import com.example.demo.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager; // inject from SecurityConfig
    private final JwtService jwtService; // our JWT util

    public AuthController(UserService userService,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register/customer")
    public ResponseEntity<?> registerCustomer(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(request, UserRole.CUSTOMER);
        if (user == null) {
            return ResponseEntity.status(409).body(
                    Map.of(
                            "status", 409,
                            "error", "Conflict",
                            "message", "Email already registered"));
        }
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name()));
    }

    @PostMapping("/register/staff")
    public ResponseEntity<?> registerStaff(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(request, UserRole.STAFF);
        if (user == null) {
            return ResponseEntity.status(409).body(
Map.of(
            "status", 409,
            "error", "Conflict",
            "message", "Email already registered"
        )
);
        }
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name()));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(request, UserRole.ADMIN);
        if (user == null) {
            return ResponseEntity.status(409).body(
Map.of(
            "status", 409,
            "error", "Conflict",
            "message", "Email already registered"
        )
);
        }
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 1) Authenticate credentials (delegates to UserDetailsService +
            // PasswordEncoder)
            var auth = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
            authenticationManager.authenticate(auth);

            // 2) Load the user (use your service or repository)
            User user = userService.findByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.status(401).body("Invalid credentials or inactive account");
            }

            // Optional: enforce status checks here if not already enforced via UserDetails
            // flags
            // if (user.getStatus() != AccountStatus.ACTIVE) { ... }

            // 3) Create JWT with useful claims
            if (user.getRole() == null) {
                return ResponseEntity.status(500).body("Internal configuration error: user has no role assigned. Please contact support.");
            }

            String token = jwtService.generateToken(
                    user.getEmail(),
                    Map.of("role", user.getRole().name(), "uid", user.getId()));

            // 4) Return token + user info
            AuthResponse response = new AuthResponse(
                    user.getId(),
                    user.getUserName(),
                    user.getEmail(),
                    user.getRole(),
                    token,
                    user.isFirstLogin(),
                    user.getPhoneNumber());

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(
Map.of(
            "status", 409,
            "error", "Conflict",
            "message", "Invalid Credentials"
        )
);
        }
    }
}