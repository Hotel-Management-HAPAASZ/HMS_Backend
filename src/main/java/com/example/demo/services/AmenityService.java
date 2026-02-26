package com.example.demo.services;

import com.example.demo.dto.AmenityDTO;
import com.example.demo.models.Amenity;
import com.example.demo.repository.AmenityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AmenityService {
    private final AmenityRepository amenityRepository;

    public List<AmenityDTO> listAll() {
        return amenityRepository.findAll().stream().map(a -> {
            AmenityDTO dto = new AmenityDTO();
            dto.setId(a.getId());
            dto.setName(a.getName());
            return dto;
        }).toList();
    }

    @Transactional
    public AmenityDTO create(AmenityDTO req) {
        amenityRepository.findByNameIgnoreCase(req.getName()).ifPresent(a -> {
            throw new RuntimeException("Amenity with the same name already exists");
        });
        Amenity a = new Amenity();
        a.setName(req.getName());
        a = amenityRepository.save(a);

        AmenityDTO dto = new AmenityDTO();
        dto.setId(a.getId());
        dto.setName(a.getName());
        return dto;
    }

    @Transactional
    public AmenityDTO update(Long id, AmenityDTO req) {
        Amenity a = amenityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Amenity not found"));

        // Optional: enforce unique name
        amenityRepository.findByNameIgnoreCase(req.getName())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> { throw new RuntimeException("Amenity with the same name already exists"); });

        a.setName(req.getName());
        a = amenityRepository.save(a);

        AmenityDTO dto = new AmenityDTO();
        dto.setId(a.getId());
        dto.setName(a.getName());
        return dto;
    }

    @Transactional
    public void delete(Long id) {
        if (!amenityRepository.existsById(id)) {
            throw new RuntimeException("Amenity not found");
        }
        amenityRepository.deleteById(id);
    }
}
