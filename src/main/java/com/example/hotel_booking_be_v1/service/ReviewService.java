package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.*;
import com.example.hotel_booking_be_v1.repository.*;
import com.example.hotel_booking_be_v1.response.ReviewResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

@Service
public class ReviewService implements IReviewService{
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ReviewPhotoRepository reviewPhotoRepository;
    private final HotelRepository hotelRepository;
    private final UserService userService;


    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository, ReviewPhotoRepository reviewPhotoRepository, HotelRepository hotelRepository, UserService userService) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.reviewPhotoRepository = reviewPhotoRepository;
        this.hotelRepository = hotelRepository;
        this.userService = userService;
    }

    public Review addReview(ReviewDTO reviewDTO, UserDetails userDetails) throws IOException, SQLException {
        // Lấy email người dùng hiện tại từ UserDetails
        String email = userDetails.getUsername();

        // Lấy thông tin người dùng từ email
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }

        // Kiểm tra nếu người dùng là chủ sở hữu của booking
        boolean isEligible = bookingRepository.existsByBookingIdAndUserIdAndStatusAndCheckOutDateBefore(
                reviewDTO.getBookingId(), user.getId(), BookingStatus.COMPLETED, LocalDate.now()
        );

        if (!isEligible) {
            throw new IllegalArgumentException("You are not eligible to review this booking.");
        }

        // Tạo đối tượng Review
        Review review = new Review();
        review.setBooking(bookingRepository.findById(reviewDTO.getBookingId())
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + reviewDTO.getBookingId())));
        review.setRating(reviewDTO.getRating());
        review.setContent(reviewDTO.getContent());

        // Lưu Review trước để có ID
        Review savedReview = reviewRepository.save(review);

        // Lưu các ảnh nếu có
        if (reviewDTO.getPhotos() != null) {
            for (MultipartFile photoFile : reviewDTO.getPhotos()) {
                ReviewPhoto photo = new ReviewPhoto();
                photo.setPhoto(new SerialBlob(photoFile.getBytes())); // Lưu ảnh dạng Blob
                photo.setReview(savedReview);
                reviewPhotoRepository.save(photo);
            }
        }

        // Cập nhật rating và starRating cho khách sạn
        Hotel hotel = savedReview.getBooking().getHotel();
        if (hotel != null) {
            int newRatingCount = hotel.getRatingCount() + 1;
            hotel.setRatingCount(newRatingCount);

            float newStarRating = reviewRepository.calculateAverageRatingByHotelId(hotel.getId());
            hotel.setStarRating(newStarRating);

            hotelRepository.save(hotel);
        }

        return savedReview;
    }

    @Override
    public List<ReviewResponse> findByHotelId(Long hotelId) {
        List<Review> reviews = reviewRepository.findByHotelId(hotelId);

        return reviews.stream().map(review -> {
            // Tạo đối tượng ReviewDTO
            ReviewResponse responseDTO = new ReviewResponse();
            responseDTO.setRating(review.getRating());
            responseDTO.setContent(review.getContent());
            responseDTO.setBookingUser(review.getBooking().getUser().getFirstName()); // Lấy thông tin người dùng từ Booking

            // Lấy danh sách ảnh dưới dạng Base64
            List<String> photoBase64List = review.getPhotos().stream()
                    .map(photo -> {
                        try {
                            Blob blob = photo.getPhoto(); // Lấy Blob
                            byte[] photoBytes = blob.getBytes(1, (int) blob.length()); // Lấy mảng byte
                            return Base64.getEncoder().encodeToString(photoBytes); // Chuyển Base64
                        } catch (SQLException e) {
                            throw new RuntimeException("Error converting Blob to Base64", e);
                        }
                    })
                    .toList();
            responseDTO.setPhotos(photoBase64List);

            return responseDTO; // Trả về đối tượng ReviewDTO
        }).toList();
    }

    @Override
    public List<Review> findByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }
}
