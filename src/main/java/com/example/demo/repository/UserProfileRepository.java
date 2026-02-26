package com.example.demo.repository;

import com.example.demo.models.UserProfile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends BaseRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);
}