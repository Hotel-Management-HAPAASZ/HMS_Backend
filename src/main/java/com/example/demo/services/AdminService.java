package com.example.demo.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.AddRoomRequest;
import com.example.demo.dto.UpdateRoomRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.billDto.CreateBillRequest;
import com.example.demo.dto.billDto.UpdateBillRequest;
import com.example.demo.dto.AdminBookingRequest;
import com.example.demo.dto.AdminUpdateBookingRequest;
import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.CreateUserResponse;
import com.example.demo.enums.AccountStatus;
import com.example.demo.enums.BookingStatus;
import com.example.demo.enums.ComplaintCategory;
import com.example.demo.enums.ComplaintStatus;
import com.example.demo.enums.Department;
import com.example.demo.enums.RoomStatus;
import com.example.demo.enums.UserRole;
import com.example.demo.models.Bill;
import com.example.demo.models.Booking;
import com.example.demo.models.Complaint;
import com.example.demo.models.Room;
import com.example.demo.models.Staff;
import com.example.demo.models.User;
import com.example.demo.repository.AmenityRepository;
import com.example.demo.repository.BillRepository;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.ComplaintRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.sepcification.RoomSpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BillRepository billRepository;
    // private final ComplaintRepository complaintRepository;
    private final PasswordEncoder passwordEncoder;
    private final AmenityRepository amenityRepository;
    private final ComplaintRepository complaintRepository;
    private final StaffRepository staffRepository;

    // Dashboard
    public Object getDashboardData() {
        long totalRooms = roomRepository.count();
        long activeRooms = roomRepository.countByStatus(RoomStatus.AVAILABLE);
        long totalBookings = bookingRepository.count();
        long openComplaints = complaintRepository.countByStatus(ComplaintStatus.OPEN);
        long totalCustomers = userRepository.countByRole(UserRole.CUSTOMER);
        long totalStaff = userRepository.countByRole(UserRole.STAFF) + userRepository.countByRole(UserRole.FOOD_STAFF);

        double occupancyRate = totalRooms > 0 ? (double) (totalRooms - activeRooms) / totalRooms * 100 : 0;

        // Revenue calculation (simple sum of all paid bookings)
        Double revenue = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.PAID || b.getStatus() == BookingStatus.CONFIRMED)
                .mapToDouble(b -> b.getTotalAmount())
                .sum();

        LocalDate today = LocalDate.now();
        long todayCheckins = bookingRepository.countByCheckInDate(today);

        return new Object() {
            public final long totalRooms = totalRooms;
            public final long activeRooms = activeRooms;
            public final long totalBookings = totalBookings;
            public final long openComplaints = openComplaints;
            public final long totalCustomers = totalCustomers;
            public final long totalStaff = totalStaff;
            public final double occupancyRate = Math.round(occupancyRate * 10) / 10.0;
            public final double revenue = revenue;
            public final long todayCheckins = todayCheckins;
        };
    }

    // Room Management
    public Page<Room> getRooms(
            String search,
            String roomType,
            RoomStatus status,
            Double minPrice,
            Double maxPrice,
            Integer maxGuest,
            List<String> amenities,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // ✅ SEARCH has highest priority
        if (search != null && !search.isBlank()) {
            return roomRepository.search(search, pageable);
        }

        Specification<Room> spec = RoomSpecification.filter(
                roomType,
                status,
                minPrice,
                maxPrice,
                maxGuest,
                amenities);

        return roomRepository.findAll(spec, pageable);
    }

    public Room addRoom(AddRoomRequest request) {

        if (request.getPricePerNight() <= 0) {
            throw new RuntimeException("Price must be greater than zero");
        }

        if (request.getMaxGuest() <= 0) {
            throw new RuntimeException("Max occupancy must be positive");
        }

        Room room = new Room();
        room.setRoomNumber(request.getRoomNumber());
        room.setFloor(request.getFloor());
        room.setRoomType(request.getRoomType());
        room.setStatus(request.getStatus());
        room.setMaxGuest(request.getMaxGuest());
        room.setPricePerNight(request.getPricePerNight());
        room.setCreatedAt(LocalDateTime.now());

        if (request.getAmenityIds() != null && !request.getAmenityIds().isEmpty()) {

            var amenities = amenityRepository.findAllById(request.getAmenityIds());

            var requested = new java.util.HashSet<>(request.getAmenityIds());
            var found = amenities.stream().map(a -> a.getId()).collect(java.util.stream.Collectors.toSet());
            requested.removeAll(found);

            if (!requested.isEmpty()) {
                throw new RuntimeException("Amenities not found: " + requested);
            }

            room.getAmenities().addAll(amenities);
        }

        return roomRepository.save(room);
    }

    public Room updateRoom(Long roomId, UpdateRoomRequest request) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        boolean hasActiveBooking = bookingRepository
                .existsByBookingRooms_Room_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                        roomId,
                        LocalDate.now(),
                        LocalDate.now());

        if (hasActiveBooking) {
            throw new RuntimeException(
                    "Room has active or upcoming reservations and cannot be updated");
        }

        if (request.getPricePerNight() != null && request.getPricePerNight() <= 0) {
            throw new RuntimeException("Invalid price");
        }

        if(request.getRoomType()!=null){
             room.setRoomType(request.getRoomType());
        }
        if(request.getMaxGuest()!=null){
             room.setMaxGuest(request.getMaxGuest());
        }
        if(request.getPricePerNight()!=null){
           room.setPricePerNight(request.getPricePerNight());
        }
        room.setUpdatedAt(LocalDateTime.now());

        if(request.getStatus()!=null){
             room.setStatus(request.getStatus());
        }




        return roomRepository.save(room);
    }

    public void deleteRoom(Long roomId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        boolean hasAnyBooking = bookingRepository
                .existsByBookingRooms_Room_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                        roomId,
                        LocalDate.now(),
                        LocalDate.now().plusYears(10));

        if (hasAnyBooking) {
            throw new RuntimeException(
                    "Room cannot be deleted because it has booking history");
        }

        roomRepository.delete(room);
    }

    // Booking Management

    // public List<Booking> getAllBookings() {
    // return bookingRepository.findAll();
    // }

    // public List<Booking> searchBookings(
    // BookingStatus status,
    // LocalDate startDate,
    // LocalDate endDate) {

    // if (status != null) {
    // return bookingRepository.findByStatus(status);
    // }

    // if (startDate != null && endDate != null) {
    // return bookingRepository.findByCheckInDateBetween(startDate, endDate);
    // }

    // return bookingRepository.findAll();
    // }

    // public Booking createBooking(AdminBookingRequest request) {

    // User user = userRepository.findById(request.getUserId())
    // .orElseThrow(() -> new RuntimeException("User not found"));

    // Room room = roomRepository.findById(request.getRoomId())
    // .orElseThrow(() -> new RuntimeException("Room not found"));

    // boolean overlap = bookingRepository
    // .existsByRoomIdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
    // room.getId(),
    // request.getCheckOutDate(),
    // request.getCheckInDate());

    // if (overlap) {
    // throw new RuntimeException("Room not available for selected dates");
    // }

    // Booking booking = new Booking();
    // booking.setUser(user);
    // booking.setRoom(room);
    // booking.setCheckInDate(request.getCheckInDate());
    // booking.setCheckOutDate(request.getCheckOutDate());
    // booking.setStatus(BookingStatus.CONFIRM);
    // booking.setAmount(request.getDepositAmount());
    // booking.setCreatedAt(LocalDateTime.now());

    // return bookingRepository.save(booking);
    // }

    // public Booking updateBooking(
    // Long bookingId,
    // AdminUpdateBookingRequest request) {

    // Booking booking = bookingRepository.findById(bookingId)
    // .orElseThrow(() -> new RuntimeException("Booking not found for the provided ID."));

    // if (request.getCheckInDate() != null) {
    // booking.setCheckInDate(request.getCheckInDate());
    // }

    // if (request.getCheckOutDate() != null) {
    // booking.setCheckOutDate(request.getCheckOutDate());
    // }

    // booking.setUpdatedAt(LocalDateTime.now());
    // return bookingRepository.save(booking);
    // }

    // public void cancelBooking(Long bookingId) {

    // Booking booking = bookingRepository.findById(bookingId)
    // .orElseThrow(() -> new RuntimeException("Booking not found for the provided ID."));

    // booking.setStatus(BookingStatus.CANCELLED);
    // booking.setUpdatedAt(LocalDateTime.now());

    // bookingRepository.save(booking);
    // }

    // usermanagement
    public Page<User> getAllUsers(String search,
            UserRole role,
            AccountStatus status,
            int page,
            int size,
            String sort) {

        // Sorting
        String[] sortParams = sort.split(",");

        // Extract field and direction
        String sortField = sortParams[0];
        Sort.Direction sortDirection = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Correct way → Sort.by(Direction, field)
        Sort sortOrder = Sort.by(sortDirection, sortField);

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // 1. Search has highest priority
        if (search != null && !search.isBlank()) {
            return userRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search, search, pageable);
        }

        // 2. Filter by role + status
        if (role != null && status != null) {
            return userRepository.findByRoleAndStatus(role, status, pageable);
        }

        // 3. Filter by role only
        if (role != null) {
            return userRepository.findByRole(role, pageable);
        }

        // 4. Filter by status only
        if (status != null) {
            return userRepository.findByStatus(status, pageable);
        }

        // 5. No filters → return all
        return userRepository.findAll(pageable);
    }

    public CreateUserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByUserName(request.getUserName())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setPhoneNumber(request.getPhone());
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setFirstLogin(true);

        // Temporary password
        String tempPassword = generateTempPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));

        User saved = userRepository.save(user);

        // Return safe DTO
        CreateUserResponse response = new CreateUserResponse();
        response.setId(saved.getId());
        response.setEmail(saved.getEmail());
        response.setUserName(saved.getUserName());
        response.setRole(saved.getRole());
        response.setTempPassword(tempPassword);

        return response;
    }

    public Staff createStaff(CreateUserRequest userRequest, String department) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByUserName(userRequest.getUserName())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUserName(userRequest.getUserName());
        user.setEmail(userRequest.getEmail());
        user.setRole(userRequest.getRole());
        user.setPhoneNumber(userRequest.getPhone());
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        // Auto-generate temporary password
        String tempPassword = generateTempPassword();
        user.setPassword(tempPassword); // No encryption for now
        user.setFirstLogin(true); // Enforce password change on first login

        User savedUser = userRepository.save(user);

        Staff staff = new Staff();
        staff.setUser(savedUser);
        staff.setDepartment(Department.valueOf(department.toUpperCase()));
        Staff savedStaff = staffRepository.save(staff);
        // Attach message for UI return
        savedUser.setPassword(
                "Temporary password: " + tempPassword +
                        ". Please change password after first login.");

        return savedStaff;
    }

    public User updateUser(Long userId, UpdateUserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Email uniqueness check (if changed)
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Username uniqueness check (if changed)
        if (!user.getUserName().equals(request.getUserName()) &&
                userRepository.existsByUserName(request.getUserName())) {
            throw new RuntimeException("Username already exists");
        }

        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhone());

        // FIX: Only update role if provided, otherwise keep existing role to avoid login crashes
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public void updateUserStatus(Long userId, boolean active) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(active ? AccountStatus.ACTIVE : AccountStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Transactional
    public String deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() == AccountStatus.INACTIVE) {
            return "User is already inactive.";
        }

        user.setStatus(AccountStatus.INACTIVE);
        user.setDeactivatedAt(LocalDateTime.now()); // optional
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Optionally: write to audit log here
        // auditService.log("USER_DEACTIVATED", userId, ...);

        return String.format("User '%s' has been deactivated successfully.", user.getUserName());
    }

    @Transactional
    public String reactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() == AccountStatus.ACTIVE) {
            return "User is already active.";
        }

        user.setStatus(AccountStatus.ACTIVE);
        user.setDeactivatedAt(null); // optional
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // auditService.log("USER_REACTIVATED", userId, ...);

        return String.format("User '%s' has been reactivated successfully.", user.getUserName());
    }

    @Transactional
    public String hardDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.deleteById(userId);

        // auditService.log("USER_HARD_DELETED", userId, ...);

        return String.format("User '%s' has been permanently deleted.", user.getUserName());
    }

    // bill section
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    public List<Bill> searchBills(Long userId) {

        if (userId != null) {
            return billRepository.findByUserId(userId);
        }

        return billRepository.findAll();
    }

    public Bill createBill(CreateBillRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Bill bill = new Bill();
        bill.setUser(user);
        bill.setTotalAmount(request.getTotalAmount());

        bill.setCreatedAt(LocalDateTime.now());

        return billRepository.save(bill);
    }

    public Bill updateBill(Long billId, UpdateBillRequest request) {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        bill.setTotalAmount(request.getTotalAmount());

        bill.setUpdatedAt(LocalDateTime.now());

        return billRepository.save(bill);
    }

    // Complaint and staff
    public List<Complaint> getAllComplaints(
            ComplaintStatus status,
            ComplaintCategory category) {

        if (status != null) {
            return complaintRepository.findByStatus(status);
        }

        if (category != null) {
            return complaintRepository.findByCategory(category);
        }

        return complaintRepository.findAll();
    }

    public void assignComplaint(Long complaintId, Long userId) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // userId comes from the User entity — look up the Staff record by user_id
        Staff staff = staffRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Staff not found for user id: " + userId));

        complaint.setAssignedStaff(staff);
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaintRepository.save(complaint);
    }

    public void updateComplaintStatus(Long complaintId, ComplaintStatus status) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        complaint.setStatus(status);

        complaintRepository.save(complaint);
    }

    // Helper
    private String generateRoomNumber() {
        return "RM" + System.currentTimeMillis();
    }

    private String generateTempPassword() {
        return "Temp@123";
    }

}