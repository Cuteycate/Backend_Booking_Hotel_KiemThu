package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.model.Booking;
import com.example.hotel_booking_be_v1.response.BookingResponse;
import com.example.hotel_booking_be_v1.model.BookingStatus;
import com.example.hotel_booking_be_v1.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;


    // Lấy thông tin chi tiết của booking
    // Lấy tất cả booking từ database
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        if (bookings.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(bookings);
    }

    // Cập nhật trạng thái của booking trong database
    @PutMapping("/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestParam BookingStatus status) {
        bookingService.updateBookingStatus(bookingId, status);
        return ResponseEntity.ok("Status updated successfully");
    }
}
