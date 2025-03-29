package com.example.hotel_booking_be_v1.repository;

import com.example.hotel_booking_be_v1.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Lấy tất cả đánh giá của một khách sạn (truy vấn thông qua Booking)
    @Query("SELECT r FROM Review r WHERE r.booking.hotel.id = :hotelId")
    List<Review> findByHotelId(@Param("hotelId") Long hotelId);

    // Lấy tất cả đánh giá của một người dùng (truy vấn thông qua Booking)
    @Query("SELECT r FROM Review r WHERE r.booking.user.id = :userId")
    List<Review> findByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.booking.hotel.id = :hotelId")
    float calculateAverageRatingByHotelId(@Param("hotelId") Long hotelId);
}


