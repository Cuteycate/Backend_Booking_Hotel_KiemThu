package com.example.hotel_booking_be_v1.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ReviewDTO {
    private Long bookingId;
    private int rating;      // Số sao
    private String content;  // Nội dung đánh giá
    private List<MultipartFile> photos; // Danh sách ảnh tải lên

    public ReviewDTO(int rating, String content, List<MultipartFile> photos, String bookingUser) {
      this.rating = rating;
      this.content = content;
      this.photos = photos;
    }

}
