package com.example.demo.repository;

import com.example.demo.enums.FoodOrderStatus;
import com.example.demo.models.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    List<FoodOrder> findByUser_IdOrderByCreatedAtDesc(Long userId);
    List<FoodOrder> findByStatusOrderByCreatedAtAsc(FoodOrderStatus status);
}


