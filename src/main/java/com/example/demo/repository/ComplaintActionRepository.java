package com.example.demo.repository;

import com.example.demo.models.ComplaintAction;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintActionRepository
        extends BaseRepository<ComplaintAction, Long> {

    List<ComplaintAction> findByComplaintIdOrderByActionAtAsc(Long complaintId);
}
