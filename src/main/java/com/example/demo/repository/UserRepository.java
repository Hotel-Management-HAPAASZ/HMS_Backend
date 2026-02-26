package com.example.demo.repository;

import com.example.demo.models.User;
import com.example.demo.enums.AccountStatus;
import com.example.demo.enums.UserRole;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User, Long> {

    Optional<User> findByEmail(String email);
    
   Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByEmailIgnoreCaseAndResetToken(String email, String resetToken);

    Optional<User> findByUserName(String userName);

    boolean existsByEmail(String email);

    boolean existsByUserName(String userName);

    List<User> findByRole(UserRole role);

    List<User> findByStatus(AccountStatus status);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String userName,
            String email,
            Pageable pageable);

    Page<User> findByRoleAndStatus(UserRole role, AccountStatus status, Pageable pageable);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByStatus(AccountStatus status, Pageable pageable);

}