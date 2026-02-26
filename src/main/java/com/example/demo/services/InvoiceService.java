package com.example.demo.services;


import com.example.demo.dto.InvoiceResponse;
import com.example.demo.models.Invoice;
import com.example.demo.models.BookingRoom;
import com.example.demo.models.Room;
import com.example.demo.repository.InvoiceRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceResponse getInvoiceByBookingId(Long bookingId) {

        Invoice invoice = invoiceRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Invoice not found for booking id: " + bookingId));

        InvoiceResponse response = new InvoiceResponse();


        // Basic Invoice Info
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setBookingId(invoice.getBooking().getId());

        // Hardcoded Hotel Info (as discussed)
        response.setHotelName("Grand Palace Hotel");
        response.setHotelAddress("MG Road, Mumbai, India");
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
        response.setRoomTypes(invoice.getBooking().getBookingRooms().stream().map(
            br -> br.getRoom().getRoomType()).distinct().collect(Collectors.toList())
        );

        // Amount Details
        response.setBaseAmount(invoice.getBaseAmount());
        response.setTaxAmount(invoice.getTaxAmount());
        response.setTotalAmount(invoice.getTotalAmount());

        // Payment Info
        response.setPaymentMethod(invoice.getPayment().getPaymentMethod().name());
        response.setTransactionId(invoice.getPayment().getTransactionId());
        response.setPaidAt(invoice.getPayment().getPaidAt());

        // Room Details
        response.setRooms(
                invoice.getBooking().getBookingRooms()
                        .stream()
                        .map(br -> {
                            InvoiceResponse.RoomDetail rd = new InvoiceResponse.RoomDetail();
                            rd.setRoomId(br.getRoom().getId());
                            rd.setRoomType(br.getRoom().getRoomType());
                            rd.setRoomPrice(br.getRoomPrice());
                            rd.setMaxGuest(br.getRoom().getMaxGuest());
                            return rd;
                        })
                        .collect(Collectors.toList())
        );

        return response;
    }
}
