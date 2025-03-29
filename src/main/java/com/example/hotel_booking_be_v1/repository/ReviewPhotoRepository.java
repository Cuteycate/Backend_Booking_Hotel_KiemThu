package com.example.hotel_booking_be_v1.repository;

import com.example.hotel_booking_be_v1.model.ReviewPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewPhotoRepository extends JpaRepository<ReviewPhoto, Long> {
    // Lấy ảnh theo review ID
    List<ReviewPhoto> findByReviewId(Long reviewId);
}
