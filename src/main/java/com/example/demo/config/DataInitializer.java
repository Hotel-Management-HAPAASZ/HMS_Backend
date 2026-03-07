package com.example.demo.config;

import com.example.demo.enums.AccountStatus;
import com.example.demo.enums.Department;
import com.example.demo.enums.UserRole;
import com.example.demo.models.Expertise;
import com.example.demo.models.Staff;
import com.example.demo.models.User;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds initial users on first startup.
 * Runs only if the user doesn't already exist (safe to leave in production).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("Admin User",    "admin@hotel.com",    "Admin@123",    "9000000001", UserRole.ADMIN,    null,                   AccountStatus.ACTIVE);
        seedUser("Staff Bill",    "staff@hotel.com",    "Staff@123",    "9000000002", UserRole.STAFF,    Expertise.BILL,         AccountStatus.ACTIVE);
        seedUser("Staff Home",    "staff2@hotel.com",   "Staff@123",    "9000000003", UserRole.STAFF,    Expertise.HOMESECRVICE, AccountStatus.ACTIVE);
        seedUser("Food Staff",    "foodstaff@hotel.com","Food@123",     "9000000005", UserRole.FOOD_STAFF, null,                 AccountStatus.ACTIVE);
        seedUser("John Customer", "customer@hotel.com", "Customer@123", "9000000004", UserRole.CUSTOMER, null,                   AccountStatus.ACTIVE);

        // Ensure Staff records exist for every STAFF-role user (idempotent)
        userRepository.findAll().stream()
            .filter(u -> u.getRole() == UserRole.STAFF || u.getRole() == UserRole.FOOD_STAFF)
            .forEach(u -> {
                if (staffRepository.findByUserId(u.getId()).isEmpty()) {
                    Staff s = new Staff();
                    s.setUser(u);
                    s.setDepartment(u.getRole() == UserRole.FOOD_STAFF ? Department.FOOD_SERVICE : Department.OTHER);
                    s.setIsActive(true);
                    staffRepository.save(s);
                    log.info("[seed] Created Staff record for user {} ({})", u.getEmail(), u.getId());
                }
            });

        log.info("=== DataInitializer complete ===");
    }

    private void seedUser(String name, String email, String rawPassword,
                          String phone, UserRole role, Expertise expertise,
                          AccountStatus status) {
        if (userRepository.findByEmail(email).isPresent()) {
            log.info("[seed] {} already exists — skipping", email);
            return;
        }
        User u = new User();
        u.setUserName(name);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setPhoneNumber(phone);
        u.setRole(role);
        u.setExpertise(expertise);
        u.setStatus(status);
        u.setFirstLogin(false);
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        userRepository.save(u);
        log.info("[seed] Created {} → {} / {}", role, email, rawPassword);
    }
}
