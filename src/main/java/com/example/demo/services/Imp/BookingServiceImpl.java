// package com.example.demo.services.Imp;


// import com.example.demo.dto.BookingRequest;
// import com.example.demo.dto.BookingResponse;
// import com.example.demo.enums.BookingStatus;
// import com.example.demo.models.*;
// import com.example.demo.repository.*;
// import com.example.demo.services.BookingService;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.time.temporal.ChronoUnit;
// import java.util.ArrayList;
// import java.util.List;

// @Service
// public class BookingServiceImpl implements BookingService {

//     private final BookingRepository bookingRepository;
//     private final BookingRoomRepository bookingRoomRepository;
//     private final RoomRepository roomRepository;
//     private final UserRepository userRepository;

//     public BookingServiceImpl(
//             BookingRepository bookingRepository,
//             BookingRoomRepository bookingRoomRepository,
//             RoomRepository roomRepository,
//             UserRepository userRepository
//     ) {
//         this.bookingRepository = bookingRepository;
//         this.bookingRoomRepository = bookingRoomRepository;
//         this.roomRepository = roomRepository;
//         this.userRepository = userRepository;
//     }

//     @Override
//     @Transactional
//     public BookingResponse createBooking(BookingRequest request) {

//         if (request.getRoomIds() == null || request.getRoomIds().isEmpty()) {
//             throw new RuntimeException("At least one room must be selected");
//         }

//         if (request.getCheckIn().isAfter(request.getCheckOut())
//                 || request.getCheckIn().isEqual(request.getCheckOut())) {
//             throw new RuntimeException("Invalid stay dates");
//         }

//         User user = userRepository.findById(request.getUserId())
//                 .orElseThrow(() -> new RuntimeException("User not found"));

//         long days = ChronoUnit.DAYS.between(
//                 request.getCheckIn(),
//                 request.getCheckOut()
//         );

//         if (days <= 0) {
//             throw new RuntimeException("Stay must be at least 1 day");
//         }

//         // Fetch all rooms first
//         List<Room> rooms = new ArrayList<>();
//         int totalCapacity = 0;

//         for (Long roomId : request.getRoomIds()) {
//             Room room = roomRepository.findById(roomId)
//                     .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));

//             rooms.add(room);
//             totalCapacity += room.getMaxGuest(); // <-- adjust getter if your field name is different
//         }

//         // Compare total capacity with requested guests
//         if (request.getNumberOfGuests() > totalCapacity) {
//             throw new RuntimeException("Selected rooms cannot accommodate requested guests");
//         }

//         Booking booking = new Booking();
//         booking.setUser(user);
//         booking.setCheckInDate(request.getCheckIn());
//         booking.setCheckOutDate(request.getCheckOut());
//         booking.setStatus(BookingStatus.CREATED);
//         booking.setCreatedAt(LocalDateTime.now());

//         double totalAmount = 0;
//         List<BookingRoom> bookingRooms = new ArrayList<>();

//         for (Room room : rooms) {

//             boolean isBooked = bookingRoomRepository.existsOverlappingBooking(
//                     room.getId(),
//                     request.getCheckIn(),
//                     request.getCheckOut()
//             );

//             if (isBooked) {
//                 throw new RuntimeException("Room not available: " + room.getId());
//             }

//             double roomPrice = room.getPricePerNight() * days;
//             totalAmount += roomPrice;

//             BookingRoom bookingRoom = new BookingRoom();
//             bookingRoom.setBooking(booking);
//             bookingRoom.setRoom(room);
//             bookingRoom.setRoomPrice(roomPrice);

//             bookingRooms.add(bookingRoom);
//         }

//         booking.setAmount(totalAmount);
//         booking.setBookingRooms(bookingRooms);

//         Booking savedBooking = bookingRepository.save(booking);

//         return mapToResponse(savedBooking);
//     }

//     private BookingResponse mapToResponse(Booking booking) {

//         List<BookingResponse.RoomSummary> rooms =
//                 booking.getBookingRooms()
//                         .stream()
//                         .map(br -> new BookingResponse.RoomSummary(
//                                 br.getRoom().getId(),
//                                 br.getRoomPrice()
//                         ))
//                         .toList();

//         BookingResponse response = new BookingResponse();
//         response.setBookingId(booking.getId());
//         response.setCheckInDate(booking.getCheckInDate());
//         response.setCheckOutDate(booking.getCheckOutDate());
//         response.setTotalAmount(booking.getAmount());
//         response.setStatus(booking.getStatus().name());
//         response.setRooms(rooms);

//         return response;
//     }
// }
