package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;


import com.example.demo.repository.AmenityRepository;
import com.example.demo.models.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/amenities")
@RequiredArgsConstructor
public class AmenityController {
    private final AmenityRepository amenityRepository;

    @GetMapping
    public List<Amenity> list() {
        return amenityRepository.findAll();
    }

    @PostMapping
    public Amenity create(@RequestBody Amenity req) {
        amenityRepository.findByNameIgnoreCase(req.getName()).ifPresent(a -> {
            throw new RuntimeException("Amenity already exists");
        });
        return amenityRepository.save(req);
    }

    @PutMapping("/{id}")
    public Amenity update(@PathVariable Long id, @RequestBody Amenity req) {
        Amenity a = amenityRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        a.setName(req.getName());
        return amenityRepository.save(a);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        amenityRepository.deleteById(id);
    }
}
