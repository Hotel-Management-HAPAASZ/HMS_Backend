package com.example.demo.sepcification;

import com.example.demo.models.Booking;
import com.example.demo.models.BookingRoom;
import com.example.demo.models.Room;
import com.example.demo.models.User;
import com.example.demo.enums.BookingStatus;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;

public class BookingSpecifications {

    public static Specification<Booking> statusEquals(String status) {
        return (root, cq, cb) -> {
            if (status == null || status.isBlank()) return cb.conjunction();
            BookingStatus st;
            try { st = BookingStatus.valueOf(status.toUpperCase()); }
            catch (Exception e) { return cb.disjunction(); } // invalid -> no results
            return cb.equal(root.get("status"), st);
        };
    }

    /** Filter by createdAt/checkInDate within [from, to] (you can pick checkInDate as primary) */
    public static Specification<Booking> checkInBetween(LocalDate from, LocalDate to) {
        return (root, cq, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            if (from != null && to != null) {
                return cb.between(root.get("checkInDate"), from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("checkInDate"), from);
            } else {
                return cb.lessThanOrEqualTo(root.get("checkInDate"), to);
            }
        };
    }

    /**
     * "query" matches booking id (string), user.fullName, or room.name
     * We join user and first room (via bookingRooms) for search.
     */
    public static Specification<Booking> queryMatches(String q) {
        return (root, cq, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();
            String like = "%" + q.toLowerCase().trim() + "%";

            var userJoin = root.join("user", JoinType.LEFT);
            var brJoin = root.join("bookingRooms", JoinType.LEFT);
            var roomJoin = brJoin.join("room", JoinType.LEFT);

            return cb.or(
                cb.like(cb.lower(cb.toString(root.get("id"))), like),
                cb.like(cb.lower(userJoin.get("fullName")), like),
                cb.like(cb.lower(roomJoin.get("roomNumber")), like),   // or room.name if you have it
                cb.like(cb.lower(roomJoin.get("roomType")), like)
            );
        };
    }
}