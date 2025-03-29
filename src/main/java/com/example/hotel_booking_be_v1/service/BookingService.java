package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.Booking;
import com.example.hotel_booking_be_v1.model.BookingStatus;
import com.example.hotel_booking_be_v1.model.Invoice;
import com.example.hotel_booking_be_v1.model.User;
import com.example.hotel_booking_be_v1.repository.BookingRepository;
import com.example.hotel_booking_be_v1.response.BookingResponse;
import com.example.hotel_booking_be_v1.response.HotelResponse;
import com.example.hotel_booking_be_v1.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService  {
    private final BookingRepository bookingRepository;
    private final IRoomService roomService;
    private final InvoiceService invoiceService; // Tiêm InvoiceService vào
    private final EmailService emailService; // Tiêm InvoiceService vào

    public void saveBooking(Booking booking) {
        bookingRepository.save(booking);
    }


    public Optional<Booking> findByBookingCodeAndUserId(String bookingCode, Long userId) {
        return bookingRepository.findByBookingCodeAndUserId(bookingCode, userId);
    }

    public List<BookingResponse> findBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .filter(booking -> booking.getStatus() == BookingStatus.COMPLETED) // Kiểm tra trạng thái
                .map(booking -> {
                    Invoice invoice = booking.getInvoice();
                    User user = booking.getUser();
                    boolean hasReview = booking.getReview() != null; // Kiểm tra review
                    return new BookingResponse(
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
                            hasReview
                    );
                })
                .collect(Collectors.toList());
    }



    public List<Booking> findBookingsByHotel(Long hotelId) {
        return bookingRepository.findByHotelId(hotelId);
    }

    public List<Booking> findBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    public List<Booking> findBookingsBetweenDates(LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findByCheckInDateBetween(startDate, endDate);
    }

    @Override
    public int countBookedRooms(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        return bookingRepository.countBookedRooms(roomId, checkInDate, checkOutDate);
    }
    // Phương thức tạo mã booking ngẫu nhiên gồm 6 chữ số
    private String generateBookingCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000; // Tạo mã từ 100000 đến 999999
        return String.valueOf(code);
    }

    @Override
    public void saveBookingWithInvoice(Booking booking) {
        Invoice invoice = invoiceService.createInvoiceForBooking(booking);
        String bookingCode = generateBookingCode();
        booking.setBookingCode(bookingCode);
        booking.setInvoice(invoice);
        // Gửi email về cho người dùng
        emailService.sendBookingConfirmation(booking.getUser().getEmail(), bookingCode);
        bookingRepository.save(booking);
    }

    @Override
    public void saveBookingWithInvoice2(Booking booking, BigDecimal depositAmount) {
        Invoice invoice = invoiceService.createInvoiceForBooking(booking);
        invoice.setDepositAmount(depositAmount);
        invoice.setPaymentMethod("VNPAY");
        invoice.setPaymentDate(new Date());
        String bookingCode = generateBookingCode();
        booking.setBookingCode(bookingCode);
        booking.setInvoice(invoice);
        // Gửi email về cho người dùng
        emailService.sendBookingConfirmation(booking.getUser().getEmail(), bookingCode);
        bookingRepository.save(booking);
    }
    @Override
    public List<Booking> findOverlappingBookings(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        return bookingRepository.findBookingsByRoomAndDateRange(roomId, checkInDate, checkOutDate);
    }

    @Override
    public List<BookingResponse> getBookingsByHotelOwnerId(Long ownerId) {
        List<Object[]> rawBookings = bookingRepository.findBookingsByHotelOwnerId(ownerId);

        // Sử dụng Map để nhóm dữ liệu theo bookingId
        Map<Long, BookingResponse> bookingMap = new LinkedHashMap<>();

        for (Object[] rawBooking : rawBookings) {
            Long bookingId = (Long) rawBooking[0];

            // Nếu bookingId đã tồn tại trong map, chỉ thêm roomName vào
            if (bookingMap.containsKey(bookingId)) {
                BookingResponse existingBooking = bookingMap.get(bookingId);
                String roomName = (String) rawBooking[7];
                if (roomName != null) {
                    existingBooking.getRoomNames().add(roomName);
                }
            } else {
                // Nếu bookingId chưa tồn tại, tạo mới BookingResponse
                BookingResponse booking = new BookingResponse();
                booking.setBookingId(bookingId);
                booking.setCheckInDate((LocalDate) rawBooking[1]);
                booking.setCheckOutDate((LocalDate) rawBooking[2]);
                booking.setStatus((BookingStatus) rawBooking[3]);
                booking.setEmail((String) rawBooking[4]);
                booking.setName((String) rawBooking[5]);
                booking.setHotelName((String) rawBooking[6]);

                // Thêm tên phòng (nếu có)
                List<String> roomNames = new ArrayList<>();
                String roomName = (String) rawBooking[7];
                if (roomName != null) {
                    roomNames.add(roomName);
                }
                booking.setRoomNames(roomNames);

                // Thêm thông tin hóa đơn
                booking.setTotalPrice((BigDecimal) rawBooking[8]);
                booking.setDepositAmount((BigDecimal) rawBooking[9]);
                booking.setPaymentDate((Date) rawBooking[10]);
                booking.setPaymentMethod((String) rawBooking[11]);

                // Lưu vào map
                bookingMap.put(bookingId, booking);
            }
        }

        // Trả về danh sách BookingResponse từ Map
        return new ArrayList<>(bookingMap.values());
    }

    public List<BookingResponse> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream().map(this::mapToBookingResponse).collect(Collectors.toList());
    }

    // Hàm chuyển đổi từ Booking entity sang BookingResponse
    private BookingResponse mapToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getId());
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setStatus(booking.getStatus());

        response.setEmail(booking.getUser() != null ? booking.getUser().getEmail() : null);

        String fullName = booking.getUser() != null
                ? (booking.getUser().getFirstName() != null ? booking.getUser().getFirstName() : "") +
                (booking.getUser().getLastName() != null ? " " + booking.getUser().getLastName() : "")
                : null;
        response.setName(fullName != null && !fullName.isBlank() ? fullName.trim() : null);

        response.setHotelName(booking.getHotel() != null ? booking.getHotel().getName() : null);
        response.setRoomNames(booking.getRooms() != null
                ? booking.getRooms().stream().map(room -> room.getName() != null ? room.getName() : "Unknown").toList()
                : null);

        response.setTotalPrice(booking.getInvoice() != null ? booking.getInvoice().getTotalPrice() : null);
        response.setDepositAmount(booking.getInvoice() != null ? booking.getInvoice().getDepositAmount() : null);
        response.setPaymentDate(booking.getInvoice() != null ? booking.getInvoice().getPaymentDate() : null);
        response.setPaymentMethod(booking.getInvoice() != null ? booking.getInvoice().getPaymentMethod() : null);

        return response;
    }


    public Booking updateBookingStatus(Long bookingId, BookingStatus status) {
        // Fetch the booking from the database
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        // Update the booking status
        booking.setStatus(status);

        // Save the updated booking back to the database
        return bookingRepository.save(booking);
    }

    public Booking cancelBooking(String bookingCode, String email) {
        // Tìm booking theo bookingCode
        Optional<Booking> bookingOptional = bookingRepository.findByBookingCode1(bookingCode);

        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();

            // Kiểm tra xem booking có phải của người dùng hiện tại không
            if (!booking.getUser().getEmail().equals(email)) {
                throw new RuntimeException("You do not have permission to cancel this booking.");
            }
            // Kiểm tra trạng thái của booking có phải PENDING hoặc DEPOSITED không
            if (!booking.getStatus().equals(BookingStatus.PENDING) && !booking.getStatus().equals(BookingStatus.DEPOSITED)) {
                throw new RuntimeException("Booking cannot be canceled. Only PENDING or DEPOSITED bookings can be canceled.");
            }

            // Cập nhật trạng thái booking thành CANCELLED
            booking.setStatus(BookingStatus.CANCELLED);

            // Lưu lại trạng thái đã cập nhật
            return bookingRepository.save(booking);
        } else {
            throw new RuntimeException("Booking not found with code: " + bookingCode);
        }
    }
}
