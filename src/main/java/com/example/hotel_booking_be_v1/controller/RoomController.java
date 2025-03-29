package com.example.hotel_booking_be_v1.controller;


import com.example.hotel_booking_be_v1.Utils.ImageUtils;
import com.example.hotel_booking_be_v1.exception.PhotoRetrievalException;
import com.example.hotel_booking_be_v1.exception.ResourceNotFoundException;
import com.example.hotel_booking_be_v1.model.*;
import com.example.hotel_booking_be_v1.repository.RoomPhotoRepository;
import com.example.hotel_booking_be_v1.repository.RoomRepository;
import com.example.hotel_booking_be_v1.response.BookingResponse;
import com.example.hotel_booking_be_v1.response.RoomResponse;
import com.example.hotel_booking_be_v1.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//@CrossOrigin("http://localhost:5173")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController {
    private final IRoomService roomService;
    private final IRoomTypeService roomTypeService;
    private final HotelService hotelService;
    private final RoomFacilityService roomFacilityService;
    private final RoomRepository roomRepository;
    private final RoomPhotoRepository roomPhotoRepository;
    private final UserService userService;

    @GetMapping("/{hotelId}")
    public ResponseEntity<List<RoomResponse>> getRoomsByHotelId(@PathVariable Long hotelId,
                                                                @AuthenticationPrincipal UserDetails userDetails) {

        // Lấy email của người dùng hiện tại từ UserDetails
        String email = userDetails.getUsername();
        User user = userService.getUserByEmail(email);

        // Kiểm tra người dùng có quyền xem thông tin khách sạn không
        Hotel hotel = hotelService.getHotelById1(hotelId); // Giả sử có hàm này
        if (hotel == null || !hotel.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // HTTP 403
        }

        // Lấy danh sách phòng theo hotelId
        List<Room> rooms = roomService.getRoomsByHotelsId(hotelId);

        List<RoomResponse> roomResponses = new ArrayList<>();

        for (Room room : rooms) {
            List<String> base64Photos = new ArrayList<>();
            for (RoomPhoto photo : room.getPhotos()) {
                byte[] photoBytes = null;
                Blob photoBlob = photo.getPhoto(); // Giả sử RoomPhoto có trường photo kiểu Blob
                if (photoBlob != null) {
                    try {
                        photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
                    } catch (SQLException e) {
                        throw new PhotoRetrievalException("Error retrieving photo for room with ID " + room.getId());
                    }
                }


                String base64Photo = photoBytes != null ? Base64.encodeBase64String(photoBytes) : null;
                if (base64Photo != null) {
                    base64Photos.add(base64Photo);
                }
            }

            // Tạo RoomResponse
            RoomResponse roomResponse = new RoomResponse(
                    room.getId(),
                    room.getName(),
                    room.getRoomPrice(),
                    room.getQuantity(),
                    room.getRoomType() != null ? room.getRoomType().getName() : "N/A", // Loại phòng
                    base64Photos, // Danh sách ảnh Base64
                    room.getDepositPercentage(),
                    room.getFacilities().stream().map(RoomFacility::getName).collect(Collectors.toList()) // Tên các tiện nghi
            );
            roomResponses.add(roomResponse);
        }

        // Trả về danh sách phòng kèm ảnh (nếu có)
        return ResponseEntity.ok(roomResponses.isEmpty() ? Collections.emptyList() : roomResponses);
    }

    @PostMapping
    public ResponseEntity<String> addRoom(@ModelAttribute RoomDTO roomDTO) {
        try {
            Hotel hotel = hotelService.getHotelById1(roomDTO.getHotelId());
            if (hotel == null) {
                return ResponseEntity.badRequest().body("Hotel not found");
            }

            // Lấy RoomType từ cơ sở dữ liệu
            RoomType roomType = roomTypeService.findRoomTypeById(roomDTO.getRoomTypeId());
            if (roomType == null) {
                return ResponseEntity.badRequest().body("Room type not found");
            }


            // Tạo Room từ RoomDTO
            Room room = new Room();
            room.setName(roomDTO.getName());
            room.setRoomPrice(roomDTO.getRoomPrice());
            room.setQuantity(roomDTO.getQuantity());
            room.setHotel(hotel);
            room.setRoomType(roomType);
            room.setDepositPercentage(roomDTO.getDepositPercentage());

            // Liên kết tiện ích
            List<RoomFacility> facilities = roomFacilityService.findByIds(roomDTO.getFacilityIds());
            room.setFacilities(facilities);

            roomRepository.save(room);
            // Xử lý và lưu ảnh
            if (roomDTO.getPhotos() != null && !roomDTO.getPhotos().isEmpty()) {
                for (MultipartFile photo : roomDTO.getPhotos()) {
                    RoomPhoto roomPhoto = new RoomPhoto();
                    roomPhoto.setPhoto(new SerialBlob(photo.getBytes()));
                    roomPhoto.setRoom(room);
                    roomPhotoRepository.save(roomPhoto);
                }
            }


            return ResponseEntity.ok("Room added successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("An error occurred while adding the room");
        }
    }

    @GetMapping("update/{roomId}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable Long roomId,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Lấy email của người dùng hiện tại từ UserDetails
            String email = userDetails.getUsername();
            User user = userService.getUserByEmail(email);

            // Kiểm tra người dùng có quyền xem thông tin khách sạn không
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
            if (room == null || !room.getHotel().getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // HTTP 403
            }


            // Hiển thị thông tin phòng hiện tại cho người dùng
            RoomResponse currentRoomResponse = new RoomResponse(
                    room.getId(),
                    room.getName(),
                    room.getRoomPrice(),
                    room.getQuantity(),
                    room.getRoomType() != null ? room.getRoomType().getName() : "N/A",

                    room.getPhotos().stream()
                            .map(photo -> {
                                try {
                                    return Base64.encodeBase64String(photo.getPhoto().getBytes(1, (int) photo.getPhoto().length()));
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .collect(Collectors.toList()),
                    room.getDepositPercentage(),
                    room.getFacilities().stream().map(RoomFacility::getName).collect(Collectors.toList()),
                    room.getFacilities().stream().collect(Collectors.toList()),
                    room.getRoomType()
            );

            return ResponseEntity.ok(currentRoomResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<String> updateRoom(@PathVariable Long roomId, @ModelAttribute RoomDTO roomDTO) {
        try {
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found"));

            // Cập nhật thông tin phòng
            room.setName(roomDTO.getName());
            room.setRoomPrice(roomDTO.getRoomPrice());
            room.setQuantity(roomDTO.getQuantity());
            room.setDepositPercentage(roomDTO.getDepositPercentage());

            // Cập nhật loại phòng
            RoomType roomType = roomTypeService.findRoomTypeById(roomDTO.getRoomTypeId());
            if (roomType == null) {
                return ResponseEntity.badRequest().body("Room type not found");
            }
            room.setRoomType(roomType);

            // Cập nhật tiện ích
            List<RoomFacility> facilities = roomFacilityService.findByIds(roomDTO.getFacilityIds());
            room.getFacilities().clear(); // Xóa liên kết cũ
            room.setFacilities(facilities); // Thêm liên kết mới
            roomRepository.save(room);

            // Xóa hình ảnh cũ
            room.getPhotos().clear();
            roomRepository.save(room);

            // Thêm hình ảnh mới
            if (roomDTO.getPhotos() != null && !roomDTO.getPhotos().isEmpty()) {
                for (MultipartFile photo : roomDTO.getPhotos()) {
                    RoomPhoto roomPhoto = new RoomPhoto();
                    roomPhoto.setPhoto(new SerialBlob(photo.getBytes()));
                    roomPhoto.setRoom(room);
                    roomPhotoRepository.save(roomPhoto);
                }
            }

            return ResponseEntity.ok("Room updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("An error occurred while updating the room");
        }
    }



}
