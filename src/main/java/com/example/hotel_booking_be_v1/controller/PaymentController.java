package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.config.Config;
import com.example.hotel_booking_be_v1.model.*;
import com.example.hotel_booking_be_v1.repository.BookingRepository;
import com.example.hotel_booking_be_v1.repository.InvoiceRepository;
import com.example.hotel_booking_be_v1.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.hotel_booking_be_v1.model.BookingStatus.DEPOSITED;
import static com.example.hotel_booking_be_v1.model.BookingStatus.PENDING;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class PaymentController {
    private final UserService userService;
    private final HotelService hotelService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final VnPayService vnPayService;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@AuthenticationPrincipal UserDetails userDetails,
                                           @ModelAttribute BookingDTO bookingDTO,
                                           HttpServletRequest request) {
        try {
            // Lấy email từ userDetails
            String email = userDetails.getUsername();


            // Step 1: Fetch the rooms by their IDs
            List<Long> roomIds = bookingDTO.getRoomIds();
            Map<Long, Room> roomMap = roomService.getRoomByIds(roomIds);

            // Step 2: Calculate the total deposit amount
            BigDecimal totalDepositAmount = BigDecimal.ZERO;

            long days = ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());

            if (days <= 0) {
                throw new IllegalArgumentException("Check-out date must be after check-in date.");
            }

            for (Long roomId : roomIds) {
                Room room = roomMap.get(roomId);  // Get room by ID
                if (room != null) {
                    // Kiểm tra nếu depositPercentage không null
                    if (room.getDepositPercentage() != null) {
                        // Calculate the deposit amount for this room
                        BigDecimal depositAmountForRoom = room.getDepositPercentage()
                                .multiply(room.getRoomPrice())  // Deposit = depositPercentage * roomPrice
                                .multiply(BigDecimal.valueOf(days));
                        totalDepositAmount = totalDepositAmount.add(depositAmountForRoom);
                    } else {
                        // Nếu không có depositPercentage, bỏ qua phòng này
                        System.out.println("Room ID " + roomId + " does not have a depositPercentage, skipping.");
                    }
                }
            }



            // Tạo URL thanh toán VNPay
            String paymentUrl = vnPayService.createPaymentUrl(request, totalDepositAmount.longValue(), bookingDTO , email);

            return ResponseEntity.ok(Map.of(
                    "status", "PENDING",
                    "message", "Booking created. Please proceed with payment.",
                    "paymentUrl", paymentUrl
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating booking.");
        }
    }


    @PostMapping("/payFull")
    public ResponseEntity<?> payFullBooking(@AuthenticationPrincipal UserDetails userDetails,
                                            @ModelAttribute BookingDTO bookingDTO,
                                            @RequestParam BigDecimal totalRoomAmount,
                                            HttpServletRequest request) {
        try {
            // Lấy email từ userDetails
            String email = userDetails.getUsername();

            // Step 1: Kiểm tra logic check-in, check-out
            long days = ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
            if (days <= 0) {
                throw new IllegalArgumentException("Check-out date must be after check-in date.");
            }

            List<Long> roomIds = bookingDTO.getRoomIds();
            Map<Long, Room> roomMap = roomService.getRoomByIds(roomIds);


            // Step 2: Calculate the total deposit amount
            BigDecimal totalDepositAmount = BigDecimal.ZERO;

            for (Long roomId : roomIds) {
                Room room = roomMap.get(roomId);  // Get room by ID
                if (room != null) {
                    // Kiểm tra nếu depositPercentage không null
                    if (room.getDepositPercentage() != null) {
                        // Calculate the deposit amount for this room
                        BigDecimal depositAmountForRoom = room.getDepositPercentage()
                                .multiply(room.getRoomPrice())  // Deposit = depositPercentage * roomPrice
                                .multiply(BigDecimal.valueOf(days));
                        totalDepositAmount = totalDepositAmount.add(depositAmountForRoom);
                    } else {
                        // Nếu không có depositPercentage, bỏ qua phòng này
                        System.out.println("Room ID " + roomId + " does not have a depositPercentage, skipping.");
                    }
                }
            }

            if (totalRoomAmount.compareTo(totalDepositAmount) < 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "FAILED",
                        "message", "Total payment amount must be greater than or equal to the deposit amount."
                ));
            }

            // Step 2: Truyền thẳng totalRoomAmount làm số tiền thanh toán
            String paymentUrl = vnPayService.createPaymentUrl(request, totalRoomAmount.longValue(), bookingDTO, email);

            return ResponseEntity.ok(Map.of(
                    "status", "PENDING",
                    "message", "Booking created with full payment. Please proceed with payment.",
                    "paymentUrl", paymentUrl
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating full payment booking.");
        }
    }



    @GetMapping("/payment/vnpay-return")
    public RedirectView handlePaymentReturn(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();

        try {
            // Lấy tất cả tham số từ yêu cầu HTTP
            Map<String, String[]> requestParams = request.getParameterMap();
            Map<String, String> vnpParams = new HashMap<>();
            requestParams.forEach((key, values) -> vnpParams.put(key, values[0])); // Chuyển các tham số thành Map<String, String>

            // Kiểm tra mã phản hồi từ VNPay
            String vnpResponseCode = vnpParams.get("vnp_ResponseCode");

            if ("00".equals(vnpResponseCode)) {
                String orderInfo = vnpParams.get("vnp_OrderInfo");

                // Bỏ "Info:" khỏi chuỗi
                String rawOrderInfo = orderInfo.replace("Info:", "").trim(); // "16?[26, 27, 27]?2025-11-30?2025-12-01?2?123@gmail.com"

                // Tách chuỗi bằng dấu "?"
                String[] parts = rawOrderInfo.split("\\?");

                // Lấy từng phần của chuỗi
                Long hotelId = Long.parseLong(parts[0]); // 16
                String roomIdsRaw = parts[1]; // "[26, 27, 27]"
                LocalDate checkInDate = LocalDate.parse(parts[2]); // 2025-11-30
                LocalDate checkOutDate = LocalDate.parse(parts[3]); // 2025-12-01
                int numberOfGuests = Integer.parseInt(parts[4]); // 2
                String userEmail = parts[5]; // "123@gmail.com"

                // Xử lý roomIds
                roomIdsRaw = roomIdsRaw.replaceAll("[\\[\\]]", ""); // Loại bỏ dấu ngoặc vuông
                List<Long> roomIds = Arrays.stream(roomIdsRaw.split(",")) // Tách từng số
                        .map(String::trim)        // Bỏ khoảng trắng
                        .map(Long::parseLong)     // Chuyển thành Long
                        .collect(Collectors.toList()); // Gom thành List<Long>

                // Find the user by email
                User user = userService.getUserByEmail(userEmail);
                if (user == null) {
                    return new RedirectView("http://localhost:5713?status=failure&message=User%20not%20found");
                }

                // Create booking
                BookingDTO bookingDTO = new BookingDTO();
                bookingDTO.setHotelId(hotelId);
                bookingDTO.setRoomIds(roomIds);
                bookingDTO.setCheckInDate(checkInDate);
                bookingDTO.setCheckOutDate(checkOutDate);
                bookingDTO.setNumberOfGuests(numberOfGuests);
                bookingDTO.setStatus(BookingStatus.DEPOSITED);

                // Create the booking from DTO
                Booking booking = new Booking();
                booking.setCheckInDate(bookingDTO.getCheckInDate());
                booking.setCheckOutDate(bookingDTO.getCheckOutDate());
                booking.setNumberOfGuests(bookingDTO.getNumberOfGuests());
                booking.setStatus(bookingDTO.getStatus());
                booking.setUser(user);

                // Find hotel by hotelId
                Hotel hotel = hotelService.getHotelById1(bookingDTO.getHotelId());
                if (hotel == null) {
                    return new RedirectView("http://localhost:5713?status=failure&message=Hotel%20not%20found");
                }
                booking.setHotel(hotel);

                // Check the availability of the rooms
                roomIds = bookingDTO.getRoomIds();
                Map<Long, Long> roomCountMap = roomIds.stream()
                        .collect(Collectors.groupingBy(roomId -> roomId, Collectors.counting()));
                List<Room> rooms = roomService.getAllRoomByIds(new ArrayList<>(roomCountMap.keySet()));

                List<Room> bookingRooms = new ArrayList<>();
                for (Map.Entry<Long, Long> entry : roomCountMap.entrySet()) {
                    Long roomId = entry.getKey();
                    Long roomCountRequested = entry.getValue();

                    // Find the room by roomId
                    Room room = rooms.stream()
                            .filter(r -> r.getId().equals(roomId))
                            .findFirst()
                            .orElse(null);

                    if (room == null) {
                        return new RedirectView("http://localhost:5713?status=failure&message=Room%20with%20ID%20" + roomId + "%20not%20found");
                    }

                    // Check the availability of the room during the booking period
                    List<Booking> overlappingBookings = bookingService.findOverlappingBookings(roomId, bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
                    long roomBookedCount = overlappingBookings.stream()
                            .flatMap(b -> b.getRooms().stream())
                            .filter(r -> r.getId().equals(roomId))
                            .count();

                    // Ensure the room is available
                    if (room.getQuantity() - roomBookedCount < roomCountRequested) {
                        return new RedirectView("http://localhost:5713?status=failure&message=Not%20enough%20rooms%20available%20for%20room%20ID%20" + roomId);
                    }

                    // Add the room to the booking list
                    for (int i = 0; i < roomCountRequested; i++) {
                        bookingRooms.add(room);
                    }
                }

                // Set the rooms for the booking
                booking.setRooms(bookingRooms);

                BigDecimal depositAmount = new BigDecimal(vnpParams.get("vnp_Amount")).divide(BigDecimal.valueOf(100)); // Convert amount from VND cents to VND

                // Save the booking
                bookingService.saveBookingWithInvoice2(booking, depositAmount);

                // Chuyển hướng sang trang thanh toán thành công
                String redirectUrl = "http://localhost:5173" +
                        "?checkInDate=" + booking.getCheckInDate() +
                        "&checkOutDate=" + booking.getCheckOutDate() +
                        "&depositAmount=" + depositAmount;

                return new RedirectView(redirectUrl);

            } else {
                // Trả về phản hồi thất bại nếu mã phản hồi không phải "00"
                return new RedirectView("http://localhost:5713?status=failure&message=Thanh%20toán%20thất%20bại.%20Vui%20lòng%20thử%20lại.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new RedirectView("http://localhost:5713?status=error&message=Đã%20xảy%20ra%20lỗi%20trong%20quá%20trình%20xử%20lý%20thanh%20toán.");
        }
    }


}

