package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.model.Review;
import com.example.hotel_booking_be_v1.model.ReviewDTO;
import com.example.hotel_booking_be_v1.response.ReviewResponse;
import com.example.hotel_booking_be_v1.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }


    @PostMapping("/add")
    public ResponseEntity<?> addReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute ReviewDTO reviewDTO) {
        try {
            Review review = reviewService.addReview(reviewDTO, userDetails);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Review added successfully with ID: " + review.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByHotel(@PathVariable Long hotelId) {
        List<ReviewResponse> reviews = reviewService.findByHotelId(hotelId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable Long userId) {
        List<Review> reviews = reviewService.findByUserId(userId);
        return ResponseEntity.ok(reviews);
    }


}
