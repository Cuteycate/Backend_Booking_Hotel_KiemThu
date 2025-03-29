package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.Booking;
import com.example.hotel_booking_be_v1.model.Room;
import com.example.hotel_booking_be_v1.model.RoomFacility;
import com.example.hotel_booking_be_v1.model.RoomPhoto;
import com.example.hotel_booking_be_v1.repository.BookingRepository;
import com.example.hotel_booking_be_v1.repository.HotelRepository;
import com.example.hotel_booking_be_v1.repository.RoomRepository;
import com.example.hotel_booking_be_v1.response.RoomResponse;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomAvailabilityService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;


    public Map<String, Object> getHotelWithAvailableRooms(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate) {


        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(hotelId, checkInDate, checkOutDate);

        // Bước 2: Đếm số lượng phòng đã được đặt
        Map<Long, Integer> roomBookedCount = new HashMap<>();
        for (Booking booking : overlappingBookings) {
            for (Room room : booking.getRooms()) {
                roomBookedCount.put(room.getId(), roomBookedCount.getOrDefault(room.getId(), 0) + 1);
            }
        }

        // Bước 3: Lấy danh sách tất cả các phòng của khách sạn
        List<Room> allRooms = roomRepository.findByHotelId(hotelId);
        List<RoomResponse> availableRooms = new ArrayList<>();

        // Bước 4: Tính số phòng còn trống và tạo RoomResponse
        for (Room room : allRooms) {
            int bookedCount = roomBookedCount.getOrDefault(room.getId(), 0);
            int availableQuantity = room.getQuantity() - bookedCount;

            if (availableQuantity > 0) { // Chỉ lấy phòng còn trống
                // Chuyển danh sách ảnh sang Base64
                List<String> base64Photos = new ArrayList<>();
                for (RoomPhoto photo : room.getPhotos()) {
                    if (photo.getPhoto() != null) {
                        try {
                            byte[] photoBytes = photo.getPhoto().getBytes(1, (int) photo.getPhoto().length());
                            String base64Photo = Base64.encodeBase64String(photoBytes);
                            base64Photos.add(base64Photo);
                        } catch (SQLException e) {
                            throw new RuntimeException("Error converting photo for room ID " + room.getId(), e);
                        }
                    }
                }

                // Lấy danh sách tên tiện nghi
                List<String> facilityNames = room.getFacilities().stream()
                        .map(RoomFacility::getName)
                        .collect(Collectors.toList());

                // Tạo RoomResponse
                RoomResponse roomResponse = new RoomResponse(
                        room.getId(),
                        room.getName(),
                        room.getRoomPrice(),
                        availableQuantity, // Số phòng còn trống
                        room.getRoomType() != null ? room.getRoomType().getName() : "N/A",
                        base64Photos, // Danh sách ảnh Base64
                        room.getDepositPercentage(),
                        facilityNames, // Tên các tiện nghi
                        room.getFacilities(), // Chi tiết tiện nghi
                        room.getRoomType() // Chi tiết loại phòng
                );

                availableRooms.add(roomResponse);
            }
        }

        // Bước 5: Tạo response trả về
        Map<String, Object> hotelData = new HashMap<>();
        if (!allRooms.isEmpty()) {
            hotelData.put("hotelId", hotelId);
            hotelData.put("hotelName", allRooms.get(0).getHotel().getName());
        } else {
            hotelData.put("hotelId", hotelId);
            hotelData.put("hotelName", "No Rooms Found");
        }
        hotelData.put("availableRooms", availableRooms);

        return hotelData;
    }
}
