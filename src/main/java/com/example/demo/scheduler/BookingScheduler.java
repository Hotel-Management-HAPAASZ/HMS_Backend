package com.example.demo.scheduler;

import com.example.demo.enums.BookingStatus;
import com.example.demo.models.Booking;
import com.example.demo.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingRepository bookingRepository;

    /**
     * Runs every hour to check for bookings that should be checked out.
     * Marks CHECKED_IN bookings as CHECKED_OUT if their checkOutDate is in the past.
     */
    @Scheduled(cron = "0 0 * * * *") // Runs at the start of every hour
    public void autoCheckoutBookings() {
        log.info("Running auto-checkout scheduler for past-due bookings...");
        LocalDate today = LocalDate.now();
        List<Booking> pastDueBookings = bookingRepository.findByStatusAndCheckOutDateBefore(BookingStatus.CHECKED_IN, today);

        if (!pastDueBookings.isEmpty()) {
            log.info("Found {} bookings to auto-checkout.", pastDueBookings.size());
            for (Booking booking : pastDueBookings) {
                booking.setStatus(BookingStatus.CHECKED_OUT);
                booking.setUpdatedAt(LocalDateTime.now());
            }
            bookingRepository.saveAll(pastDueBookings);
            log.info("Successfully updated {} bookings to CHECKED_OUT status.", pastDueBookings.size());
        } else {
            log.info("No past-due CHECKED_IN bookings found.");
        }
    }
}
