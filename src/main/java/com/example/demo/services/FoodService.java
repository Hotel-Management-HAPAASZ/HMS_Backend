package com.example.demo.services;

import com.example.demo.dto.food.*;
import com.example.demo.enums.FoodOrderStatus;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.models.*;
import com.example.demo.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodItemRepository foodItemRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final FoodPaymentRepository foodPaymentRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public List<FoodMenuItemResponse> getMenu() {
        return foodItemRepository.findAll().stream()
                .sorted(Comparator.comparing(FoodItem::getCategory, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(FoodItem::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(i -> new FoodMenuItemResponse(
                        i.getId(),
                        i.getName(),
                        i.getDescription(),
                        i.getCategory(),
                        i.getPrice(),
                        i.getAvailable()
                ))
                .toList();
    }

    @Transactional
    public FoodPaymentResponse createOrderAndInitiatePayment(@Valid CreateFoodOrderRequest request) {
        if (request.getPaymentMethod() != PaymentMethod.CARD) {
            throw new RuntimeException("Only CARD payments are supported for food pay-now in this version.");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        Booking activeBooking;

        if (request.getBookingId() != null) {
            // Use the specified booking
            activeBooking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found for the provided ID."));

            // Validate ownership
            if (!activeBooking.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Booking does not belong to the specified user.");
            }

            // Validate status and dates
            if (activeBooking.getStatus() != com.example.demo.enums.BookingStatus.CHECKED_IN) {
                throw new RuntimeException("Food ordering is only available for checked-in bookings.");
            }
            if (activeBooking.getCheckInDate().isAfter(today) || activeBooking.getCheckOutDate().isBefore(today)) {
                throw new RuntimeException("Food ordering is only available during an active stay (between check-in and check-out dates).");
            }
        } else {
            // Auto-select only if exactly one checked-in booking is active; otherwise force selection
            List<Booking> activeBookings = bookingRepository.findAllActiveStayBookingsForUser(user.getId(), today);
            if (activeBookings.isEmpty()) {
                throw new RuntimeException("Food ordering is only available during an active stay.");
            }
            if (activeBookings.size() > 1) {
                throw new RuntimeException("Multiple checked-in rooms found. Please select a room to order food.");
            }
            activeBooking = activeBookings.get(0);
        }

        // Load and validate menu items
        Map<Long, FoodItem> itemMap = new HashMap<>();
        for (FoodOrderItemRequest line : request.getItems()) {
            FoodItem item = foodItemRepository.findById(line.getFoodItemId())
                    .orElseThrow(() -> new RuntimeException("Food item not found: " + line.getFoodItemId()));
            if (Boolean.FALSE.equals(item.getAvailable())) {
                throw new RuntimeException("Food item is not available: " + item.getName());
            }
            itemMap.put(item.getId(), item);
        }

        FoodOrder order = new FoodOrder();
        order.setUser(user);
        order.setBooking(activeBooking);
        order.setStatus(FoodOrderStatus.AWAITING_PAYMENT);
        order.setTotalAmount(0.0);

        // Build items + totals
        double total = 0.0;
        List<FoodOrderItem> orderItems = new ArrayList<>();
        for (FoodOrderItemRequest line : request.getItems()) {
            FoodItem item = itemMap.get(line.getFoodItemId());
            int qty = line.getQuantity();
            double unit = item.getPrice() == null ? 0.0 : item.getPrice();
            double lineTotal = unit * qty;
            total += lineTotal;

            FoodOrderItem oi = new FoodOrderItem();
            oi.setFoodOrder(order);
            oi.setFoodItem(item);
            oi.setQuantity(qty);
            oi.setUnitPrice(unit);
            oi.setLineTotal(lineTotal);
            orderItems.add(oi);
        }
        order.setTotalAmount(total);
        order.setItems(orderItems);

        FoodOrder savedOrder = foodOrderRepository.save(order);

        FoodPayment payment = new FoodPayment();
        payment.setFoodOrder(savedOrder);
        payment.setAmount(savedOrder.getTotalAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setStatus(PaymentStatus.INITIATED);
        // Demo OTP (same pattern as booking payments)
        payment.setOtp("111111");
        payment.setOtpGeneratedAt(LocalDateTime.now());

        FoodPayment savedPayment = foodPaymentRepository.save(payment);

        FoodPaymentResponse resp = new FoodPaymentResponse();
        resp.setPaymentId(savedPayment.getId());
        resp.setOrderId(savedOrder.getId());
        resp.setAmount(savedPayment.getAmount());
        resp.setPaymentMethod(savedPayment.getPaymentMethod());
        resp.setPaymentStatus(savedPayment.getStatus());
        resp.setPaidAt(savedPayment.getPaidAt());
        resp.setMessage("OTP sent successfully");
        return resp;
    }

    @Transactional
    public FoodPaymentResponse verifyPayment(@Valid FoodPaymentVerifyRequest request) {
        FoodPayment payment = foodPaymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Food payment not found"));

        if (payment.getStatus() != PaymentStatus.INITIATED) {
            throw new RuntimeException("Food payment not eligible for verification");
        }

        if (payment.getOtp() == null) {
            throw new RuntimeException("OTP not generated");
        }

        if (!payment.getOtp().equals(request.getOtp())) {
            payment.setStatus(PaymentStatus.FAILED);
            foodPaymentRepository.save(payment);
            FoodPaymentResponse resp = new FoodPaymentResponse();
            resp.setPaymentId(payment.getId());
            resp.setOrderId(payment.getFoodOrder().getId());
            resp.setAmount(payment.getAmount());
            resp.setPaymentMethod(payment.getPaymentMethod());
            resp.setPaymentStatus(payment.getStatus());
            resp.setPaidAt(payment.getPaidAt());
            resp.setMessage("Invalid OTP");
            return resp;
        }

        // Success
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        foodPaymentRepository.save(payment);

        FoodOrder order = payment.getFoodOrder();
        order.setStatus(FoodOrderStatus.NEW);
        // 10-min ETA from successful payment
        order.setExpectedDeliveryAt(LocalDateTime.now().plusMinutes(10));
        foodOrderRepository.save(order);

        FoodPaymentResponse resp = new FoodPaymentResponse();
        resp.setPaymentId(payment.getId());
        resp.setOrderId(order.getId());
        resp.setAmount(payment.getAmount());
        resp.setPaymentMethod(payment.getPaymentMethod());
        resp.setPaymentStatus(payment.getStatus());
        resp.setPaidAt(payment.getPaidAt());
        resp.setMessage("Payment successful");
        return resp;
    }

    @Transactional(readOnly = true)
    public List<FoodOrderResponse> myOrders(Long userId) {
        return foodOrderRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FoodOrderResponse> pendingOrders() {
        // show both paid-new and in-progress to kitchen
        List<FoodOrder> result = new ArrayList<>();
        result.addAll(foodOrderRepository.findByStatusOrderByCreatedAtAsc(FoodOrderStatus.NEW));
        result.addAll(foodOrderRepository.findByStatusOrderByCreatedAtAsc(FoodOrderStatus.PREPARING));
        result.addAll(foodOrderRepository.findByStatusOrderByCreatedAtAsc(FoodOrderStatus.ON_THE_WAY));
        return result.stream().map(this::toOrderResponse).toList();
    }

    @Transactional
    public FoodOrderResponse updateOrderStatus(Long orderId, FoodOrderStatus status) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Food order not found"));

        if (order.getStatus() == FoodOrderStatus.AWAITING_PAYMENT) {
            throw new RuntimeException("Cannot update status until payment is completed.");
        }

        order.setStatus(status);
        FoodOrder saved = foodOrderRepository.save(order);
        return toOrderResponse(saved);
    }

    // Menu management (for FOOD_STAFF / ADMIN)
    @Transactional
    public FoodMenuItemResponse upsertMenuItem(Long id, FoodMenuItemResponse payload) {
        FoodItem item = (id == null)
                ? new FoodItem()
                : foodItemRepository.findById(id).orElseThrow(() -> new RuntimeException("Food item not found"));

        item.setName(payload.getName());
        item.setDescription(payload.getDescription());
        item.setCategory(payload.getCategory());
        item.setPrice(payload.getPrice());
        item.setAvailable(payload.getAvailable() != null ? payload.getAvailable() : Boolean.TRUE);

        FoodItem saved = foodItemRepository.save(item);
        return new FoodMenuItemResponse(saved.getId(), saved.getName(), saved.getDescription(), saved.getCategory(), saved.getPrice(), saved.getAvailable());
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        foodItemRepository.deleteById(id);
    }

    // Get all checked-in bookings for a user (for room selection)
    @Transactional(readOnly = true)
    public List<CheckedInBookingResponse> getCheckedInBookings(Long userId) {
        LocalDate today = LocalDate.now();
        List<Booking> bookings = bookingRepository.findAllActiveStayBookingsForUser(userId, today);

        return bookings.stream().map(b -> {
            List<String> roomNumbers = b.getBookingRooms().stream()
                    .map(br -> br.getRoom().getRoomNumber())
                    .collect(Collectors.toList());
            return new CheckedInBookingResponse(
                    b.getId(),
                    roomNumbers,
                    b.getCheckInDate().toString(),
                    b.getCheckOutDate().toString()
            );
        }).collect(Collectors.toList());
    }

    private FoodOrderResponse toOrderResponse(FoodOrder o) {
        List<FoodOrderResponse.FoodOrderLineResponse> lines = o.getItems().stream()
                .map(li -> new FoodOrderResponse.FoodOrderLineResponse(
                        li.getFoodItem().getId(),
                        li.getFoodItem().getName(),
                        li.getQuantity(),
                        li.getUnitPrice(),
                        li.getLineTotal()
                ))
                .toList();

        List<String> roomNumbers = Collections.emptyList();
        Long bookingId = null;
        if (o.getBooking() != null) {
            bookingId = o.getBooking().getId();
            try {
                if (o.getBooking().getBookingRooms() != null) {
                    roomNumbers = o.getBooking().getBookingRooms().stream()
                            .filter(br -> br != null && br.getRoom() != null && br.getRoom().getRoomNumber() != null)
                            .map(br -> br.getRoom().getRoomNumber())
                            .toList();
                }
            } catch (Exception ignored) {
                // If lazy loading fails in some environments, keep empty list
                roomNumbers = Collections.emptyList();
            }
        }

        return new FoodOrderResponse(
                o.getId(),
                bookingId,
                roomNumbers,
                o.getStatus(),
                o.getTotalAmount(),
                o.getExpectedDeliveryAt(),
                o.getCreatedAt(),
                lines
        );
    }
}


