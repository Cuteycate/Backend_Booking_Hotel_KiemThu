package com.example.hotel_booking_be_v1.controller;


import com.example.hotel_booking_be_v1.model.*;
import com.example.hotel_booking_be_v1.repository.BookingRepository;
import com.example.hotel_booking_be_v1.repository.UserRepository;
import com.example.hotel_booking_be_v1.response.BookingResponse;
import com.example.hotel_booking_be_v1.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.file.attribute.UserPrincipal;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

//@CrossOrigin("http://localhost:5173")
@RequiredArgsConstructor
@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final IBookingService bookingService;
    private final IRoomService roomService;
    private final IUserService userService;
    private final IHotelService hotelService;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;


    @PostMapping("/add")
    public ResponseEntity<?> addBooking(@AuthenticationPrincipal UserDetails userDetails,
                                        @ModelAttribute BookingDTO bookingDTO) {
        try {
            // Lấy email từ userDetails
            String email = userDetails.getUsername();//1

            // Tìm user bằng email
            User user = userService.getUserByEmail(email);//2
            if (user == null) {//3
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            // Tạo đối tượng Booking từ BookingDTO
            Booking booking = new Booking();//4

            booking.setCheckInDate(bookingDTO.getCheckInDate());
            booking.setCheckOutDate(bookingDTO.getCheckOutDate());
            booking.setNumberOfGuests(bookingDTO.getNumberOfGuests());
            booking.setStatus(bookingDTO.getStatus());
            booking.setUser(user); // Gán User lấy từ token


            // Lấy đối tượng Hotel từ hotelId trong DTO
            Hotel hotel = hotelService.getHotelById1(bookingDTO.getHotelId());//5
            if (hotel == null) {//6
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hotel not found.");
            }
            booking.setHotel(hotel);

            List<Long> roomIds = bookingDTO.getRoomIds();//7

            // Đếm số lần xuất hiện của từng roomId trong roomIds
            Map<Long, Long> roomCountMap = roomIds.stream()
                    .collect(Collectors.groupingBy(roomId -> roomId, Collectors.counting()));//8

            // Lấy danh sách phòng từ cơ sở dữ liệu
            List<Room> rooms = roomService.getAllRoomByIds(new ArrayList<>(roomCountMap.keySet()));//9

            // Kiểm tra số lượng phòng có sẵn cho từng loại phòng (theo roomIds)
            List<Room> bookingRooms = new ArrayList<>();//9
            for (Map.Entry<Long, Long> entry : roomCountMap.entrySet()) { //10
                Long roomId = entry.getKey();//11
                Long roomCountRequested = entry.getValue();//11

                // Tìm phòng theo roomId
                Room room = rooms.stream()
                        .filter(r -> r.getId().equals(roomId))
                        .findFirst()
                        .orElse(null);//11

                if (room == null) {
                    //12
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room with ID " + roomId + " not found.");
                }

                // Kiểm tra số lượng phòng đã được đặt trong khoảng thời gian check-in và check-out
                List<Booking> overlappingBookings = bookingService.findOverlappingBookings(roomId
                        , bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());//13

                long roomBookedCount = overlappingBookings.stream()
                        .flatMap(b -> b.getRooms().stream())
                        .filter(r -> r.getId().equals(roomId))
                        .count();//14

                // Kiểm tra số lượng phòng còn trống
                if (room.getQuantity() - roomBookedCount < roomCountRequested) {//15
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Not enough rooms available for room ID " + roomId + ". Only " + (room.getQuantity() - roomBookedCount) + " rooms available.");
                }

                // Thêm phòng vào danh sách bookingRooms
                for (int i = 0; i < roomCountRequested; i++) {//16
                    bookingRooms.add(room);//17
                }
            }
            booking.setRooms(bookingRooms);//18

            // Tính số ngày thuê và đảm bảo check-out phải sau check-in
            long days = ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(),
                    bookingDTO.getCheckOutDate());//18

            if (days <= 0) {//19
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Check-out date must be after check-in date.");
            }

            // Lưu Booking cùng với hóa đơn (Invoice)
            bookingService.saveBookingWithInvoice(booking);//20

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Booking created successfully with invoice.");//21
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }   
    @GetMapping("/user")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@AuthenticationPrincipal UserDetails userDetails) {
        // Lấy email từ userDetails
        String email = userDetails.getUsername();

        // Tìm user bằng email
        User user = userService.getUserByEmail(email);
        List<BookingResponse> bookings = bookingService.findBookingsByUser(user.getId());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/user/booking")
    public ResponseEntity<BookingResponse> getBookingByCode(@RequestParam String bookingCode, @AuthenticationPrincipal UserDetails userDetails) {
        // Lấy email từ userDetails
        String email = userDetails.getUsername();

        // Tìm user bằng email
        User user = userService.getUserByEmail(email);

        // Tìm booking dựa trên booking_code và userId
        Optional<Booking> bookingOpt = bookingService.findByBookingCodeAndUserId(bookingCode, user.getId());

        // Kiểm tra nếu booking không tồn tại
        if (!bookingOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Booking booking = bookingOpt.get();
        Invoice invoice = booking.getInvoice();
        BookingResponse bookingResponse = new BookingResponse(
                booking.getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getStatus(),
                user != null ? user.getEmail() : null,
                user != null ? user.getFirstName() : null,
                booking.getHotel().getName(),

                booking.getRooms().stream().map(room -> room.getName()).collect(Collectors.toList()),
                 invoice != null ? invoice.getTotalPrice() : BigDecimal.ZERO,
                invoice != null ? invoice.getDepositAmount() : BigDecimal.ZERO,
                invoice != null ? invoice.getPaymentDate() : null,
                invoice != null ? invoice.getPaymentMethod() : null,
                false

        );

        return ResponseEntity.ok(bookingResponse);
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<Booking>> getBookingsByHotel(@PathVariable Long hotelId) {
        return ResponseEntity.ok(bookingService.findBookingsByHotel(hotelId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Booking>> getBookingsByStatus(@PathVariable BookingStatus status) {
        return ResponseEntity.ok(bookingService.findBookingsByStatus(status));
    }
    @GetMapping("/hotel-owner/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingsByHotelOwner(@AuthenticationPrincipal UserDetails userDetails) {
        String ownerEmail = userDetails.getUsername(); // Email lấy từ token

        User user = userService.getUserByEmail(ownerEmail);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long ownerId = user.getId(); // Lấy Owner ID từ đối tượng Owner
        List<BookingResponse> bookings = bookingService.getBookingsByHotelOwnerId(ownerId);
        return ResponseEntity.ok(bookings);
    }

    //xac nhan cho rental
    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String bookingCode) {
        try {
            // Lấy userId từ thông tin người dùng đã đăng nhập (token)
            String email = userDetails.getUsername();
            User owner = userService.getUserByEmail(email);
            if (owner == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }
            // Tìm booking theo mã đặt phòng
            Booking booking = bookingRepository.findByBookingCode(bookingCode);
            if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found.");
            }
            // Kiểm tra xem người yêu cầu có phải là chủ khách sạn của booking hay không
            if (!booking.getHotel().getOwner().getId().equals(owner.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to check in this booking.");
            }
            // Kiểm tra trạng thái hiện tại của booking
            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Booking is not confirmed.");
            }

            // Cập nhật trạng thái thành COMPLETED khi check-in
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);

            // Chuyển đổi Booking thành BookingResponse
            BookingResponse bookingResponse = new BookingResponse();
            bookingResponse.setBookingId(booking.getId());
            bookingResponse.setCheckInDate(booking.getCheckInDate());
            bookingResponse.setCheckOutDate(booking.getCheckOutDate());
            bookingResponse.setStatus(booking.getStatus());
            bookingResponse.setEmail(booking.getUser().getEmail()); // Giả sử user có trường email
            bookingResponse.setName(booking.getUser().getFirstName()); // Giả sử user có trường name
            bookingResponse.setHotelName(booking.getHotel().getName()); // Giả sử hotel có trường name
            bookingResponse.setRoomNames(booking.getRooms().stream()
                    .map(room -> room.getName()) // Giả sử Room có trường name
                    .collect(Collectors.toList()));
            bookingResponse.setTotalPrice(booking.getInvoice().getTotalPrice()); // Giả sử Booking có trường totalPrice
            bookingResponse.setDepositAmount(booking.getInvoice().getDepositAmount()); // Giả sử Booking có trường depositAmount
            bookingResponse.setPaymentDate(booking.getInvoice().getPaymentDate()); // Giả sử Booking có trường paymentDate
            bookingResponse.setPaymentMethod(booking.getInvoice().getPaymentMethod()); // Giả sử Booking có trường paymentMethod

            return ResponseEntity.status(HttpStatus.OK).body(bookingResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/confirm")
    @Transactional
    public ResponseEntity<String> confirmBooking(
            @RequestParam Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User owner = userService.getUserByEmail(email);

        // Tìm booking theo bookingId
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (!bookingOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found!");
        }

        Booking booking = bookingOptional.get();
        Hotel hotel = booking.getHotel();

        // Kiểm tra xem người dùng đã đăng nhập có phải là chủ sở hữu khách sạn hay không
        if (hotel.getOwner().getId().equals(owner.getId())) {
            // Kiểm tra trạng thái của booking có phải là PENDING hoặc DEPOSITED
            if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.DEPOSITED) {
                // Cập nhật trạng thái booking thành CONFIRMED
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
                // Gửi email xác nhận
                String customerEmail = booking.getUser().getEmail(); // Lấy email người đã booking
                String subject = "Booking Confirmed!";
                String message = String.format(
                        "Dear %s,\n\nYour booking with ID %d at %s has been confirmed successfully. We look forward to welcoming you.\n\nBest regards,\n%s",
                        booking.getUser().getFirstName(),
                        booking.getId(),
                        hotel.getName(),
                        hotel.getOwner().getFirstName()
                );

                emailService.sendMail(customerEmail, subject, message);
                return ResponseEntity.status(HttpStatus.OK).body("Booking confirmed successfully!");
            } else {
                // Trả về lỗi nếu trạng thái không phải PENDING hoặc DEPOSITED
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Booking status is not valid for confirmation!");
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to confirm this booking!");
        }
    }
    @GetMapping("/user/cancel")
    public ResponseEntity<String> cancelBooking(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String bookingCode) {
        try {
            // Lấy email từ userDetails và gọi service cancelBooking
            String email = userDetails.getUsername();
            User user = userService.getUserByEmail(email);
            bookingService.cancelBooking(bookingCode, user.getEmail());
            return ResponseEntity.ok("Booking has been cancelled successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Booking not found or could not be cancelled: " + e.getMessage());
        }
    }


}
