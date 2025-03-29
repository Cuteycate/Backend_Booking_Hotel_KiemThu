package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.service.RoomAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/hotels")
public class RoomAvailabilityController {

    @Autowired
    private RoomAvailabilityService roomAvailabilityService;

    @GetMapping("/{hotelId}/available-rooms")
    public ResponseEntity<Map<String, Object>> getAvailableRooms(
            @PathVariable Long hotelId,
            @RequestParam String checkInDate,
            @RequestParam String checkOutDate) {

        LocalDate checkIn = LocalDate.parse(checkInDate);
        LocalDate checkOut = LocalDate.parse(checkOutDate);

        Map<String, Object> response = roomAvailabilityService.getHotelWithAvailableRooms(hotelId, checkIn, checkOut);
        return ResponseEntity.ok(response);
    }
}
