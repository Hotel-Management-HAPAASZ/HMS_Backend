package com.example.demo.repository;

import com.example.demo.models.Invoice;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends BaseRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByBooking_Id(Long bookingId);

    boolean existsByBooking_Id(Long bookingId);
}