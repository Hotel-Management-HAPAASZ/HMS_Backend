package com.example.demo.services;

import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.dto.VerifyOtpRequest;
import com.example.demo.models.User;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Hardcoded OTP for demo
    private static final String DEMO_OTP = "999999";
    // Settings
    private static final int OTP_TTL_MIN = 10;
    private static final int OTP_MAX_ATTEMPTS = 5;
    private static final int RESET_TOKEN_TTL_MIN = 15;

    public void requestForgotPassword(ForgotPasswordRequest req) {
        Optional<User> maybeUser = userRepository.findByEmailIgnoreCase(req.getEmail());
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            user.setOtpCode(DEMO_OTP);
            user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(OTP_TTL_MIN));
            user.setOtpAttempts(0);
            // Clear previous reset token
            user.setResetToken(null);
            user.setResetTokenExpiresAt(null);
            userRepository.save(user);

            // In a real app, send OTP via email/SMS here
            // For demo, log it or just rely on hardcoded knowledge
        }

        // Always return success to prevent user enumeration
    }

    public String verifyOtp(VerifyOtpRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or OTP"));

        // Check attempts
        Integer attempts = user.getOtpAttempts() == null ? 0 : user.getOtpAttempts();
        if (attempts >= OTP_MAX_ATTEMPTS) {
            throw new IllegalStateException("OTP attempts exceeded. Request a new OTP.");
        }

        // Validate expiry
        if (user.getOtpExpiresAt() == null || user.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("OTP expired. Request a new OTP.");
        }

        // Validate code (hardcoded)
        if (!DEMO_OTP.equals(req.getOtp())) {
            user.setOtpAttempts(attempts + 1);
            userRepository.save(user);
            throw new IllegalArgumentException("Invalid OTP");
        }

        // Success → issue reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_TTL_MIN));

        // Clear OTP after success to avoid reuse
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        user.setOtpAttempts(0);

        userRepository.save(user);

        return resetToken; // Return to client (in real app, send via secure channel)
    }

    public void resetPassword(ResetPasswordRequest req) {
        User user = userRepository.findByEmailIgnoreCaseAndResetToken(req.getEmail(), req.getResetToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token or email"));

        if (user.getResetTokenExpiresAt() == null || user.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Reset token expired. Restart the process.");
        }

        // Basic password checks (example)
        if (req.getNewPassword() == null || req.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());

        // Invalidate the token
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);

        userRepository.save(user);
    }
}