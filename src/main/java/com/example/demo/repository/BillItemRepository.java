package com.example.demo.repository;

import com.example.demo.models.BillItem;
import org.springframework.stereotype.Repository;

@Repository
public interface BillItemRepository extends BaseRepository<BillItem, Long> {
}