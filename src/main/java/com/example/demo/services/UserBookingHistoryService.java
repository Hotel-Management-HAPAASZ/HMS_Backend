package com.example.demo.services;


import com.example.demo.dto.UserBookingHistoryResponse;
import com.example.demo.models.Booking;
import com.example.demo.models.Payment;


import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserBookingHistoryService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;


    public List<UserBookingHistoryResponse> getUserBookings(Long userId) {

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingRepository.findByUserId(userId);

        return bookings.stream().map(booking -> {

            UserBookingHistoryResponse response = new UserBookingHistoryResponse();

            response.setBookingId(booking.getId());


            response.setCheckIn(booking.getCheckInDate());
            response.setCheckOut(booking.getCheckOutDate());
            response.setNumberOfGuests(booking.getNumberOfGuests());

            response.setBookingStatus(booking.getStatus().name());
            response.setTotalAmount(booking.getAmount());

            List<String> roomNumbers = booking.getBookingRooms()
                    .stream()
                    .map(br -> br.getRoom().getRoomNumber())
                    .collect(Collectors.toList());

            response.setRoomNumbers(roomNumbers);

            // Extract room types
            List<String> roomTypes = booking.getBookingRooms()
                    .stream()
                    .map(br -> br.getRoom().getRoomType())
                    .collect(Collectors.toList());

            response.setRoomTypes(roomTypes);

            // Get payment status
            Payment payment = paymentRepository.findTopByBooking_IdOrderByIdDesc(booking.getId()).orElse(null);

            if (payment != null) {
                response.setPaymentStatus(payment.getStatus().name());
            } else {
                response.setPaymentStatus("PENDING");
            }

            return response;

        }).collect(Collectors.toList());
    }

    // Admin
    public List<UserBookingHistoryResponse> getAllBookings(){
        List<Booking> bookings = bookingRepository.findAll();

        return bookings.stream().map(this::mapToResponse).toList();
    }

    private UserBookingHistoryResponse mapToResponse(Booking booking){
        UserBookingHistoryResponse response = new UserBookingHistoryResponse();

        response.setBookingId(booking.getId());

        response.setCheckIn(booking.getCheckInDate());
        response.setCheckOut(booking.getCheckOutDate());
        response.setTotalAmount(booking.getAmount());
        response.setNumberOfGuests(booking.getNumberOfGuests());
        response.setBookingStatus((booking.getStatus().name()));

        Payment payment = paymentRepository.findTopByBooking_IdOrderByIdDesc(booking.getId()).orElse(null);

        if(payment != null){
            response.setPaymentStatus(payment.getStatus().name());
        } else{
            response.setPaymentStatus("PENDING");
        }

        response.setRoomTypes(booking.getBookingRooms().stream().map(br -> br.getRoom().getRoomType()).toList());
        response.setRoomNumbers(booking.getBookingRooms().stream().map(br -> br.getRoom().getRoomNumber()).toList());

        return response;
    }
}
