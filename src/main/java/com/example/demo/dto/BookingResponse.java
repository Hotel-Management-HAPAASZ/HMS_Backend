package com.example.demo.dto;


import java.time.LocalDate;
import java.util.List;

public class BookingResponse {
    private Long bookingId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Double totalAmount;
    private String status;
    private List<RoomSummary> rooms;

    public static class RoomSummary{
        private Long roomId;
        private Double roomPrice;

        public RoomSummary(Long roomId, Double roomPrice){
            this.roomId = roomId;
            this.roomPrice = roomPrice;
        }


        public Long getRoomId() {
            return roomId;
        }

        public Double getRoomPrice() {
            return roomPrice;
        }

    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDate getCheckInDate() {
        return checkIn;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkIn = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOut;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOut = checkOutDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<RoomSummary> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomSummary> rooms) {
        this.rooms = rooms;
    }

    
}
