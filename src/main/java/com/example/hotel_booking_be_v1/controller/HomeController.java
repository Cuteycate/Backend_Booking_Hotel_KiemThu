package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.model.Hotel;
import com.example.hotel_booking_be_v1.model.Room;
import com.example.hotel_booking_be_v1.model.RoomDTO;
import com.example.hotel_booking_be_v1.model.RoomFacility;
import com.example.hotel_booking_be_v1.service.IBookingService;
import com.example.hotel_booking_be_v1.service.IHotelService;
import com.example.hotel_booking_be_v1.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api")
public class HomeController {
    private final LocationService locationService;
    private final IHotelService hotelService;
    private final IBookingService bookingService;

    @Autowired
    public HomeController(LocationService locationService, IHotelService hotelService, IBookingService bookingService) {
        this.locationService = locationService;
        this.hotelService = hotelService;
        this.bookingService = bookingService;
    }

    @GetMapping("/auto-complete")
    public ResponseEntity<?> getLocation(@RequestParam String query) {
        try {
            List<Map<String, String>> locations = locationService.searchLocations(query);
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/search/rooms")
    public ResponseEntity<?> getRoomsValid(
            @RequestParam LocalDate inDate,
            @RequestParam LocalDate outDate,
            @RequestParam String locationType,
            @RequestParam Long locationCode, // ID của tỉnh/huyện/xã
            @RequestParam int adult) {

        try {
            // 1. Tìm khách sạn theo địa chỉ (locationId)
            List<Hotel> hotels = hotelService.findHotelsByLocation(locationCode, locationType);
            if (hotels.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No hotels found in the given location.");
            }

            // 2. Lọc các phòng có loại phòng hỗ trợ đủ số lượng người lớn (adult)
            List<Room> validRooms = hotels.stream()
                    .flatMap(hotel -> hotel.getRooms().stream())
                    .filter(room -> room.getRoomType().getAdults() >= adult)
                    .toList();

            // 3. Kiểm tra số lượng phòng còn hợp lệ dựa trên thời gian đặt
            List<RoomDTO> availableRooms = validRooms.stream()
                    .map(room -> {
                        // Lấy số lượng phòng đã được đặt trong thời gian inDate - outDate
                        int bookedRooms = bookingService.countBookedRooms(
                                room.getId(), inDate, outDate);

                        // Số phòng còn lại
                        int validCount = room.getQuantity() - bookedRooms;

                        if (validCount > 0) {
                            // Chuyển đổi Room sang RoomDTO
                            RoomDTO roomDTO = new RoomDTO();
                            roomDTO.setId(room.getId());
                            roomDTO.setName(room.getName());
                            roomDTO.setRoomPrice(room.getRoomPrice());
                            roomDTO.setQuantity(validCount);
                            roomDTO.setHotelId(room.getHotel().getId());
                            roomDTO.setRoomTypeId(room.getRoomType().getId());
                            roomDTO.setFacilityIds(
                                    room.getFacilities().stream()
                                            .map(RoomFacility::getId)
                                            .collect(Collectors.toList())
                            );
                            roomDTO.setValid(validCount);
                            return roomDTO;
                        }
                        return null; // Loại bỏ các phòng không hợp lệ
                    })
                    .filter(Objects::nonNull) // Loại bỏ null
                    .collect(Collectors.toList());

            return ResponseEntity.ok(availableRooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred: " + e.getMessage());
        }
    }

}
