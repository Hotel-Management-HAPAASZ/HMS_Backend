// src/main/java/com/example/demo/services/InvoiceService.java
package com.example.demo.services;

import com.example.demo.dto.InvoiceResponse;
import com.example.demo.models.Invoice;
import com.example.demo.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import com.example.demo.models.Payment;
import com.example.demo.models.Booking;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByBookingId(Long bookingId) {

        Invoice invoice = invoiceRepository.findByBooking_Id(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Invoice not found for booking id: " + bookingId));

        InvoiceResponse response = new InvoiceResponse();

        // Basic Invoice Info
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setBookingId(invoice.getBooking().getId());

        // Hotel Info
        response.setHotelName("Grand Palace Hotel");
        response.setHotelAddress("ABC Road, Pune, India");
        response.setHotelEmail("support@grandpalace.com");
        response.setHotelSupportNumber("+91-9876543210");

        // Customer Info
        response.setCustomerName(invoice.getBooking().getUser().getUserName());
        response.setCustomerEmail(invoice.getBooking().getUser().getEmail());
        response.setCustomerMobile(invoice.getBooking().getUser().getPhoneNumber());
        response.setChildren(invoice.getBooking().getChildren());
        response.setAdults(invoice.getBooking().getAdults());

        // Stay Details
        response.setCheckInDate(invoice.getBooking().getCheckInDate());
        response.setCheckOutDate(invoice.getBooking().getCheckOutDate());
        response.setNumberOfGuests(invoice.getBooking().getNumberOfGuests());
        response.setRoomTypes(
                invoice.getBooking().getBookingRooms().stream()
                        .map(br -> br.getRoom().getRoomType())
                        .distinct()
                        .collect(Collectors.toList())
        );

        // Amount Details
        response.setBaseAmount(invoice.getBaseAmount());
        response.setTaxAmount(invoice.getTaxAmount());
        response.setTotalAmount(invoice.getTotalAmount());
        // Force service charges to 0.0 for now
        response.setServiceCharges(0.0);

        // Payment Info
        response.setPaymentMethod(invoice.getPayment().getPaymentMethod().name());
        response.setTransactionId(invoice.getPayment().getTransactionId());
        response.setPaidAt(invoice.getPayment().getPaidAt());

        // Room Details (populate roomNumber for each line)
        response.setRooms(
                invoice.getBooking().getBookingRooms()
                        .stream()
                        .map(br -> {
                            InvoiceResponse.RoomDetail rd = new InvoiceResponse.RoomDetail();
                            rd.setRoomId(br.getRoom().getId());
                            rd.setRoomType(br.getRoom().getRoomType());
                            rd.setRoomPrice(br.getRoomPrice());
                            rd.setMaxGuest(br.getRoom().getMaxGuest());
                            rd.setRoomNumber(br.getRoom().getRoomNumber()); // <-- room number here
                            return rd;
                        })
                        .collect(Collectors.toList())
        );

        // Also provide a combined list of room numbers at top-level
        response.setRoomNumbers(
                invoice.getBooking().getBookingRooms()
                        .stream()
                        .map(br -> br.getRoom().getRoomNumber())
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList())
        );

        return response;
    }

    public void createInvoice(Payment payment) {
        Booking booking = payment.getBooking();

        invoiceRepository.findByBooking_Id(booking.getId()).ifPresent(existingInvoice -> {
            invoiceRepository.delete(existingInvoice);
            invoiceRepository.flush();
        });

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8));
        invoice.setBooking(booking);
        invoice.setPayment(payment);
        invoice.setBaseAmount(booking.getAmount());
        invoice.setTaxAmount(0.0);
        invoice.setTotalAmount(booking.getAmount());
        invoice.setGeneratedAt(LocalDateTime.now());

        invoiceRepository.save(invoice);
    }
}
