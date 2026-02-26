// package com.example.demo.services.Imp;


// import com.example.demo.dto.PaymentStartRequest;
// import com.example.demo.dto.PaymentVerifyRequest;
// import com.example.demo.dto.PaymentResponse;
// import com.example.demo.enums.BookingStatus;
// import com.example.demo.enums.PaymentMethod;
// import com.example.demo.enums.PaymentStatus;
// import com.example.demo.models.Booking;
// import com.example.demo.models.Invoice;
// import com.example.demo.models.Payment;
// import com.example.demo.repository.BookingRepository;
// import com.example.demo.repository.InvoiceRepository;
// import com.example.demo.repository.PaymentRepository;
// import com.example.demo.services.PaymentService;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.Random;
// import java.util.UUID;

// @Service
// public class PaymentServiceImpl implements PaymentService {

//     private final BookingRepository bookingRepository;
//     private final PaymentRepository paymentRepository;
//     private final InvoiceRepository invoiceRepository;

//     public PaymentServiceImpl(
//             BookingRepository bookingRepository,
//             PaymentRepository paymentRepository,
//             InvoiceRepository invoiceRepository
//     ) {
//         this.bookingRepository = bookingRepository;
//         this.paymentRepository = paymentRepository;
//         this.invoiceRepository = invoiceRepository;
//     }

//     @Override
//     @Transactional
//     public PaymentResponse initiatePayment(PaymentStartRequest request) {

//         Booking booking = bookingRepository.findById(request.getBookingId())
//                 .orElseThrow(() ->
//                         new IllegalArgumentException("Booking not found with id: " + request.getBookingId())
//                 );

//         if (!BookingStatus.CREATED.equals(booking.getStatus())) {
//             throw new IllegalStateException(
//                     "Booking is not eligible for payment. Current status: " + booking.getStatus()
//             );
//         }

//         // Prevent duplicate payment for same booking
//         if (paymentRepository.existsByBookingId(booking.getId())) {
//             throw new IllegalStateException("Payment already exists for this booking");
//         }

//         Payment payment = new Payment();
//         payment.setBooking(booking);
//         payment.setAmount(booking.getAmount());
//         payment.setPaymentMethod(request.getPaymentMethod());
//         payment.setTransactionId(UUID.randomUUID().toString());

//         if (PaymentMethod.CARD.equals(request.getPaymentMethod())) {

//             payment.setStatus(PaymentStatus.INITIATED);

//             String otp = String.valueOf(new Random().nextInt(900000) + 100000);
//             payment.setOtp(otp);
//             payment.setOtpGeneratedAt(LocalDateTime.now());

//         } else {
//             payment.setStatus(PaymentStatus.PENDING);
//         }

//         Payment savedPayment = paymentRepository.save(payment);

//         return buildResponse(
//                 savedPayment,
//                 PaymentMethod.CARD.equals(request.getPaymentMethod())
//                         ? "OTP sent successfully"
//                         : "Payment pending (pay at hotel)"
//         );
//     }

//     @Override
//     @Transactional
//     public PaymentResponse verifyPayment(PaymentVerifyRequest request) {

//         Payment payment = paymentRepository.findById(request.getPaymentId())
//                 .orElseThrow(() ->
//                         new IllegalArgumentException("Payment not found with id: " + request.getPaymentId())
//                 );

//         if (!PaymentStatus.INITIATED.equals(payment.getStatus())) {
//             throw new IllegalStateException(
//                     "Payment not eligible for verification. Current status: " + payment.getStatus()
//             );
//         }

//         if (payment.getOtp() == null) {
//             throw new IllegalStateException("OTP not generated for this payment");
//         }

//         if (!payment.getOtp().equals(request.getOtp())) {

//             payment.setStatus(PaymentStatus.FAILED);
//             paymentRepository.save(payment);

//             return buildResponse(payment, "Invalid OTP");
//         }

//         payment.setStatus(PaymentStatus.SUCCESS);
//         payment.setPaidAt(LocalDateTime.now());

//         Booking booking = payment.getBooking();
//         booking.setStatus(BookingStatus.CONFIRM);

//         generateInvoice(payment);

//         paymentRepository.save(payment);

//         return buildResponse(payment, "Payment successful");
//     }

//     private void generateInvoice(Payment payment) {

//         Booking booking = payment.getBooking();

//         Invoice invoice = new Invoice();
//         invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8));
//         invoice.setBooking(booking);
//         invoice.setPayment(payment);
//         invoice.setBaseAmount(booking.getAmount());
//         invoice.setTaxAmount(0.0);
//         invoice.setTotalAmount(booking.getAmount());
//         invoice.setGeneratedAt(LocalDateTime.now());

//         invoiceRepository.save(invoice);
//     }

//     private PaymentResponse buildResponse(Payment payment, String message) {

//         PaymentResponse response = new PaymentResponse();
//         response.setPaymentId(payment.getId());
//         response.setBookingId(payment.getBooking().getId());
//         response.setAmount(payment.getAmount());
//         response.setPaymentMethod(payment.getPaymentMethod());
//         response.setPaymentStatus(payment.getStatus());
//         response.setPaidAt(payment.getPaidAt());
//         response.setMessage(message);

//         return response;
//     }
// }
