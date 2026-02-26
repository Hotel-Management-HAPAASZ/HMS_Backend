package com.example.demo.services;


import com.example.demo.dto.PaymentResponse;
import com.example.demo.dto.PaymentStartRequest;
import com.example.demo.dto.PaymentVerifyRequest;
import com.example.demo.enums.BookingStatus;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.models.Booking;
import com.example.demo.models.Invoice;
import com.example.demo.models.Payment;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;



    @Transactional
    public PaymentResponse initiatePayment(PaymentStartRequest request) {

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() ->
                        new RuntimeException("Booking not found")
                );

        // Booking must be CREATED or PENDING (modification case)
        if (!(BookingStatus.CREATED.equals(booking.getStatus())
                || BookingStatus.PENDING.equals(booking.getStatus()))) {

            throw new RuntimeException(
                    "Booking not eligible for payment. Status: " + booking.getStatus()
            );
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setRefundAmount(0.0);
        payment.setModified(false);

        if (PaymentMethod.CARD.equals(request.getPaymentMethod())) {

            payment.setStatus(PaymentStatus.INITIATED);

            String otp = String.valueOf(new Random().nextInt(900000) + 100000);
            payment.setOtp("111111");
            payment.setOtpGeneratedAt(LocalDateTime.now());

        } else {
            payment.setStatus(PaymentStatus.PENDING);
        }

        Payment savedPayment = paymentRepository.save(payment);

        return buildResponse(
                savedPayment,
                PaymentMethod.CARD.equals(request.getPaymentMethod())
                        ? "OTP sent successfully"
                        : "Payment pending (pay at hotel)"
        );
    }

  

    @Transactional
    public PaymentResponse verifyPayment(PaymentVerifyRequest request) {

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() ->
                        new RuntimeException("Payment not found")
                );

        if (!PaymentStatus.INITIATED.equals(payment.getStatus())) {
            throw new RuntimeException("Payment not eligible for verification");
        }

        if (payment.getOtp() == null) {
            throw new RuntimeException("OTP not generated");
        }

        // OTP WRONG
        if (!payment.getOtp().equals(request.getOtp())) {

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            return buildResponse(payment, "Invalid OTP");
        }

        // OTP CORRECT
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CONFIRMED);

        bookingRepository.save(booking);
        paymentRepository.save(payment);

        generateInvoice(payment);

        return buildResponse(payment, "Payment successful");
    }

  

    private void generateInvoice(Payment payment) {

        Booking booking = payment.getBooking();

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-" +
                UUID.randomUUID().toString().substring(0, 8));

        invoice.setBooking(booking);
        invoice.setPayment(payment);
        invoice.setBaseAmount(payment.getAmount());
        invoice.setTaxAmount(0.0);
        invoice.setTotalAmount(payment.getAmount());
        invoice.setGeneratedAt(LocalDateTime.now());

        invoiceRepository.save(invoice);
    }



    private PaymentResponse buildResponse(Payment payment, String message) {

        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getId());
        response.setBookingId(payment.getBooking().getId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentStatus(payment.getStatus());
        response.setPaidAt(payment.getPaidAt());
        response.setMessage(message);

        return response;
    }
}
