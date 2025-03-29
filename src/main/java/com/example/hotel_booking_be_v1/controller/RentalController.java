package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.exception.UserAlreadyExistsException;
import com.example.hotel_booking_be_v1.model.User;
import com.example.hotel_booking_be_v1.service.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rental")
public class RentalController {
    @Autowired
    private RentalService rentalService;

    @PostMapping("/register")
    public ResponseEntity<?> registerRental(@RequestBody User user) {
        try {
            rentalService.registerRental(user);
            return ResponseEntity.ok("Rental account registered. Waiting for admin approval.");
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> approveRental(@PathVariable Long id) {
        try {
            rentalService.approveRental(id);
            return ResponseEntity.ok("Rental account approved successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<User>> getAllRentals() {
        List<User> rentals = rentalService.getAllRentals();
        return ResponseEntity.ok(rentals);
    }
}
