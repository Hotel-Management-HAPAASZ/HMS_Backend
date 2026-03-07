package com.example.demo.repository;

import com.example.demo.models.FoodPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodPaymentRepository extends JpaRepository<FoodPayment, Long> {
}


