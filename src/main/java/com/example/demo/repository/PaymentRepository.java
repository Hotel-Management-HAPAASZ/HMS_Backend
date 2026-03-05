package com.example.demo.repository;

import com.example.demo.models.Payment;
import com.example.demo.models.Booking;
import com.example.demo.enums.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends BaseRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByBooking(Booking booking);
    Optional<Payment> findByBooking_Id(Long bookingId);
    Optional<Payment> findTopByBooking_IdAndStatusOrderByIdDesc(Long bookingId, PaymentStatus status);
    Optional<Payment> findTopByBooking_IdOrderByIdDesc(Long bookingId);

    List<Payment> findByStatus(PaymentStatus status);

    boolean existsByBooking_IdAndStatus(Long bookingId, PaymentStatus status);
    boolean existsByBooking_Id(Long bookingId);

}
