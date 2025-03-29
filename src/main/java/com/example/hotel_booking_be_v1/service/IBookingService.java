package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.Booking;
import com.example.hotel_booking_be_v1.model.BookingStatus;
import com.example.hotel_booking_be_v1.response.BookingResponse;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IBookingService {
//    void cancelBooking(Long bookingId);

//    String saveBooking(Long roomId, BookedRoom bookingRequest);

//    BookedRoom findByBookingConfirmationCode(String confirmationCode);

//    List<BookedRoom> getAllBookings();
//    List<BookedRoom> getBookingsByUserEmail(String email);
    void saveBooking(Booking booking);

    List<BookingResponse> findBookingsByUser(Long userId);

    List<Booking> findBookingsByHotel(Long hotelId);

    List<Booking> findBookingsByStatus(BookingStatus status);

    List<Booking> findBookingsBetweenDates(LocalDate startDate, LocalDate endDate);

    int countBookedRooms(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);

    void saveBookingWithInvoice(Booking booking);
    void saveBookingWithInvoice2(Booking booking, BigDecimal depositAmount);
    List<Booking> findOverlappingBookings(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);
    List<BookingResponse> getBookingsByHotelOwnerId(Long ownerId);

    Optional<Booking> findByBookingCodeAndUserId(String bookingCode, Long id);

    Booking cancelBooking(String bookingCode, String email);
}
