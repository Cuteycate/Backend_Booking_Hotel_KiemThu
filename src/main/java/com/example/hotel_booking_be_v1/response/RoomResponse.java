package com.example.hotel_booking_be_v1.response;

import com.example.hotel_booking_be_v1.model.RoomFacility;
import com.example.hotel_booking_be_v1.model.RoomType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;

import java.math.BigDecimal;
import java.util.List;
@Data
@NoArgsConstructor
public class RoomResponse {
    private Long id;
    private String name; // Tên của room
    private BigDecimal roomPrice; // Giá phòng
    private int quantity; // Số lượng phòng
    private String roomType; // Tên loại phòng
    private RoomType roomTypeDetails;
    private List<String> photos; // Danh sách ảnh (Base64)
    private List<String> facilities; // Danh sách tên các tiện nghi của room //này để truyền name thôi
    private BigDecimal depositPercentage;
    private List<RoomFacility> facilityDetails; // Thay vì List<String>, sử dụng List<FacilityDTO> // này để truyền cả ID theo name để update

    // Constructor cơ bản
    public RoomResponse(Long id, String name, BigDecimal roomPrice, int quantity, String roomType, List<String> photos, BigDecimal depositPercentage, List<String> facilities) {
        this.id = id;
        this.name = name;
        this.roomPrice = roomPrice;
        this.quantity = quantity;
        this.roomType = roomType;
        this.photos = photos;
        this.depositPercentage = depositPercentage;
        this.facilities = facilities;
    }
    public RoomResponse(Long id, String name, BigDecimal roomPrice, int quantity, String roomType, List<String> photos, BigDecimal depositPercentage ,List<String> facilities,List<RoomFacility> facilityDetails, RoomType roomTypeDetails) {
        this.id = id;
        this.name = name;
        this.roomPrice = roomPrice;
        this.quantity = quantity;
        this.roomType = roomType;
        this.photos = photos;
        this.depositPercentage = depositPercentage;
        this.facilityDetails = facilityDetails;
        this.roomTypeDetails = roomTypeDetails;
    }

}
