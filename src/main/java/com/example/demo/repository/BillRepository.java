package com.example.demo.repository;

import com.example.demo.models.Bill;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends BaseRepository<Bill, Long> {

    List<Bill> findByUserId(Long userId);
}