package com.example.demo.repository;

import com.example.demo.models.Amenity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AmenityRepository extends BaseRepository<Amenity, Long> {

    Optional<Amenity> findByName(String name);
      Optional<Amenity> findByNameIgnoreCase(String name);
}