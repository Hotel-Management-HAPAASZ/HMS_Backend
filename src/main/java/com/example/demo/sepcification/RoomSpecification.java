package com.example.demo.sepcification;

import org.springframework.data.jpa.domain.Specification;

import com.example.demo.enums.RoomStatus;
import com.example.demo.models.Room;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RoomSpecification {

    public static Specification<Room> filter(
            String roomType,
            RoomStatus status,
            Double minPrice,
            Double maxPrice,
            Integer maxGuest,
            List<String> amenities) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Room type (exact match; make it case-insensitive if you want)
            if (roomType != null) {
                // predicates.add(cb.equal(root.get("roomType"), roomType));
                // For case-insensitive match, replace with:
                predicates.add(cb.equal(cb.lower(root.get("roomType")), roomType.toLowerCase()));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

        
            if (minPrice != null && maxPrice != null) {
                predicates.add(cb.between(root.get("pricePerNight"), minPrice, maxPrice));
            } else if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerNight"), minPrice));
            } else if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerNight"), maxPrice));
            }

            if (maxGuest != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxGuest"), maxGuest));
            }

   
            if (amenities != null && !amenities.isEmpty()) {

                Set<String> required = amenities.stream()
                        .filter(s -> s != null && !s.trim().isEmpty())
                        .map(s -> s.toLowerCase().trim())
                        .collect(Collectors.toCollection(HashSet::new));

                if (!required.isEmpty()) {
                   
                    Subquery<Long> sq = query.subquery(Long.class);
                    Root<Room> r2 = sq.from(Room.class);
                    Join<Object, Object> a2 = r2.join("amenities");

                    sq.select(r2.get("id"))
                      .where(
                          cb.equal(r2.get("id"), root.get("id")),
                          cb.lower(a2.get("name")).in(required)
                      )
                      .groupBy(r2.get("id"))
                      .having(
                          cb.equal(cb.countDistinct(cb.lower(a2.get("name"))), (long) required.size())
                      );

                   
                    predicates.add(cb.in(root.get("id")).value(sq));
                }
            }

            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}