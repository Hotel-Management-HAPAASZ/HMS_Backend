// package com.example.demo.services.Imp;

// import com.example.demo.dto.InvoiceResponse;
// import com.example.demo.models.Invoice;
// import com.example.demo.repository.InvoiceRepository;
// import com.example.demo.services.InvoiceService;

// import org.springframework.stereotype.Service;

// import java.util.stream.Collectors;

// @Service
// public class InvoiceServiceImpl implements InvoiceService {

//     private final InvoiceRepository invoiceRepository;

//     public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
//         this.invoiceRepository = invoiceRepository;
//     }

//     @Override
//     public InvoiceResponse getInvoiceByBookingId(Long bookingId) {

//         Invoice invoice = invoiceRepository.findByBookingId(bookingId)
//                 .orElseThrow(() -> new RuntimeException("Invoice not found"));

//         InvoiceResponse response = new InvoiceResponse();

//         response.setInvoiceNumber(invoice.getInvoiceNumber());
//         response.setBookingId(invoice.getBooking().getId());
//         response.setUserName(invoice.getBooking().getUser().getUserName());
//         response.setCheckInDate(invoice.getBooking().getCheckInDate());
//         response.setCheckOutDate(invoice.getBooking().getCheckOutDate());
//         response.setBaseAmount(invoice.getBaseAmount());
//         response.setTaxAmount(invoice.getTaxAmount());
//         response.setTotalAmount(invoice.getTotalAmount());
//         response.setPaidAt(invoice.getPayment().getPaidAt());

//         response.setRooms(
//                 invoice.getBooking().getBookingRooms()
//                         .stream()
//                         .map(br -> {
//                             InvoiceResponse.RoomDetail rd = new InvoiceResponse.RoomDetail();
//                             rd.setRoomId(br.getRoom().getId());
//                             rd.setRoomPrice(br.getRoomPrice());
//                             return rd;
//                         })
//                         .collect(Collectors.toList())
//         );

//         return response;
//     }
// }