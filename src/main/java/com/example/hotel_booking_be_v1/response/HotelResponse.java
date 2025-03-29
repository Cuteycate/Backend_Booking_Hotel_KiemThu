package com.example.hotel_booking_be_v1.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class HotelResponse {
    private Long id;
    private String name;
    private String description;
    private String photo;  // Chuỗi ảnh Base64
    private List<String> photos; // Danh sách ảnh phụ dưới dạng Base64
    private String status;  // Trạng thái khách sạn
    private String email;   // Email của khách sạn
    private String phoneNumber; // Số điện thoại của khách sạn
    private String street; // Địa chỉ của khách sạn
    private String wardName; // Tên phường
    private String districtName; // Tên quận
    private String provinceName; // Tên tỉnh
    private List<String> facilityNames;  // Thêm tiện ích vào đây
    private int ratingCount; // Số lượng đánh giá
    private float starRating; // Xếp hạng sao
    private BigDecimal cheapestRoomPrice;

    public HotelResponse(Long id, String name, String description, String photo, String status, String email, String phoneNumber, String street, String wardName, String districtName, String provinceName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.photo = photo;
        this.status = status;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.street = street;
        this.wardName = wardName;
        this.districtName = districtName;
        this.provinceName = provinceName;
    }
    public HotelResponse(Long id, String name, String description, String photo, List<String> photos, String status, String email, String phoneNumber, String street, String wardName, String districtName, String provinceName,  List<String> facilityNames) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.photo = photo;
        this.photos = photos;
        this.status = status;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.street = street;
        this.wardName = wardName;
        this.districtName = districtName;
        this.provinceName = provinceName;
        this.facilityNames = facilityNames;
    }

    public HotelResponse(Long id, String name, String description, String photo, List<String> photos, String status, String email, String phoneNumber, String street, String wardName, String districtName, String provinceName,  List<String> facilityNames, int ratingCount, float starRating ,BigDecimal cheapestRoomPrice ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.photo = photo;
        this.photos = photos;
        this.status = status;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.street = street;
        this.wardName = wardName;
        this.districtName = districtName;
        this.provinceName = provinceName;
        this.facilityNames = facilityNames;
        this.ratingCount = ratingCount;
        this.starRating = starRating;
        this.cheapestRoomPrice = cheapestRoomPrice;
    }

}

