package com.example.hotel_booking_be_v1.model;

import jakarta.annotation.Nullable;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomDTO {
    @Nullable
    private Long id;
    private String name;
    private BigDecimal roomPrice;
    private int quantity;
    private long hotelId;
    private long roomTypeId;
    private List<Long> facilityIds;
    private List<MultipartFile> Photos; // Danh sách ảnh dạng Base64
    @Nullable
    private Integer valid;
    private BigDecimal depositPercentage;
}
