package com.example.hotel_booking_be_v1.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data

@NoArgsConstructor
public class ReviewResponse {
    private int rating;      // Số sao
    private String content;  // Nội dung đánh giá
    private List<String> photos; // Danh sách ảnh tải lên
    private String bookingUser;  // Tên hoặc thông tin người đặt phòng

    public ReviewResponse(int rating, String content, List<String> photos, String bookingUser) {
        this.rating = rating;
        this.content = content;
        this.photos = photos;
        this.bookingUser = bookingUser;
    }
}
