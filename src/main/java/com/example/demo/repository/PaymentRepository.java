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
    Optional<Payment> findByBookingId(Long bookingId);
    Optional<Payment> findTopByBookingIdAndStatusOrderByIdDesc(Long bookingId, PaymentStatus status);
    Optional<Payment> findTopByBookingIdOrderByIdDesc(Long bookingId);

    List<Payment> findByStatus(PaymentStatus status);

    boolean existsByBookingIdAndStatus(Long bookingId, PaymentStatus status);
    boolean existsByBookingId(Long bookingId);

}
