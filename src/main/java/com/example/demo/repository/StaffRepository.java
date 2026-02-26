package com.example.demo.repository;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.models.Staff;
import com.example.demo.enums.*;

@Repository
public interface StaffRepository extends JpaRepository <Staff,Long>
{
      Optional<Staff> findByUserId(Long userId);
      List<Staff> findByDepartment(Department department);
      List<Staff> findByIsActive(Boolean isActive);
}