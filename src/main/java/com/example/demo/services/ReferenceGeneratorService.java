package com.example.demo.services;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferenceGeneratorService {

    private final JdbcTemplate jdbcTemplate;

    public String generateComplaintReference() {
        // Fallback or explicit query if seq doesn't exist natively.
        // H2/MySQL approach for sequence/table.
        // If 'complaint_seq' sequence doesn't exist, this will throw an exception.
        // A safer programmatic way for existing DBs during demo:
        try {
            Long seq = jdbcTemplate.queryForObject("SELECT nextval('complaint_seq')", Long.class);
            int year = Year.now().getValue();
            return String.format("CMP-%d-%04d", year, seq != null ? seq : 1L);
        } catch (Exception e) {
            // Fallback for demo if sequence is not yet created in the DB:
            // Counting total rows + 1 (Not perfectly thread-safe, but works for demo)
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM complaints", Long.class);
            int year = Year.now().getValue();
            return String.format("CMP-%d-%04d", year, (count != null ? count : 0) + 1);
        }
    }
}
