// src/main/java/com/example/demo/repository/ComplaintRepository.java
package com.example.demo.repository;

import com.example.demo.models.Complaint;
import com.example.demo.enums.ComplaintStatus;
import com.example.demo.enums.ComplaintCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends BaseRepository<Complaint, Long> {

    Optional<Complaint> findByReferenceNumber(String referenceNumber);

    Page<Complaint> findByUser_Id(Long userId, Pageable pageable);
    Page<Complaint> findByUser_IdAndStatus(Long userId, ComplaintStatus status, Pageable pageable);

    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);
    List<Complaint> findByStatus(ComplaintStatus status);
    List<Complaint> findByCategory(ComplaintCategory category);
    List<Complaint> findByAssignedStaff_Id(Long staffId);
}