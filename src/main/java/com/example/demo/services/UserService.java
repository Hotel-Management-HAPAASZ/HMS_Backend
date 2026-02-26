package com.example.demo.services;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.enums.AccountStatus;
import com.example.demo.enums.UserRole;
import com.example.demo.models.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // inject the encoder

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterRequest request, UserRole role) {
        // Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            return null;
        }

        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());

        // ✅ ENCODE password before saving
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setPhoneNumber(request.getPhoneNumber());
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(role);
        user.setStatus(AccountStatus.ACTIVE);
        user.setFirstLogin(true); // optional if you track first login

        return userRepository.save(user);
    }

    /**
     * Validates credentials:
     * - email must exist
     * - password must match (BCrypt)
     * - account must be ACTIVE
     * Returns the user (with password cleared) or null if invalid.
     */
    public User loginUser(LoginRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();

        // ✅ Match with encoder (don't compare plain text)
        boolean pwdOk = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!pwdOk) {
            return null;
        }

        if (user.getStatus() != AccountStatus.ACTIVE) {
            return null;
        }

        // Hide password before returning
        user.setPassword(null);
        return user;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}