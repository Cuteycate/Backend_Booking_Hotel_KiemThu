package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.exception.PhotoRetrievalException;
import com.example.hotel_booking_be_v1.model.*;
import com.example.hotel_booking_be_v1.repository.UserRepository;
import com.example.hotel_booking_be_v1.response.HotelResponse;
import com.example.hotel_booking_be_v1.service.DistrictService;
import com.example.hotel_booking_be_v1.service.HotelService;
import com.example.hotel_booking_be_v1.service.ProvinceService;
import com.example.hotel_booking_be_v1.service.WardService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hotels")
public class HotelController {
    private final HotelService hotelService;
    private final ProvinceService provinceService;
    private final DistrictService districtService;
    private final WardService wardService;
    private final UserRepository userRepository;

    // Đăng ký Hotel mới
    // Rental đăng ký Hotel mới
    @PostMapping("/register")
    @PreAuthorize("hasRole('ROLE_RENTAL')")
    public ResponseEntity<Hotel> registerHotel(
            @ModelAttribute HotelDTO hotelDTO,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException, SQLException {
        String ownerEmail = userDetails.getUsername();
        Hotel registeredHotel = hotelService.addHotel(hotelDTO, ownerEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredHotel);
    }
    @PutMapping("/hotels/update/{id}")
    public ResponseEntity<?> updateHotel(
            @PathVariable Long id,
            @ModelAttribute HotelDTO hotelDTO) {
        try {
            // Tìm khách sạn theo ID
            Hotel updatedHotel = hotelService.updateHotel(id, hotelDTO);

            return ResponseEntity.ok(updatedHotel);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating hotel: " + e.getMessage());
        }
    }
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<HotelResponse>> getPendingHotels() {
        try {
            // Lấy danh sách khách sạn có trạng thái "PENDING"
            List<Hotel> pendingHotels = hotelService.getHotelsByStatus("PENDING");
            if (pendingHotels.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            List<HotelResponse> hotelResponses = new ArrayList<>();

            // Xử lý từng khách sạn
            for (Hotel hotel : pendingHotels) {
                List<String> encodedPhotos = new ArrayList<>();

                // Mã hóa ảnh phụ thành Base64
                if (hotel.getPhotos() != null && !hotel.getPhotos().isEmpty()) {
                    encodedPhotos = hotel.getPhotos().stream()
                            .filter(photo -> photo.getPhoto() != null)  // Bỏ qua ảnh null
                            .map(photo -> encodePhoto(photo.getPhoto()))  // Mã hóa ảnh thành Base64
                            .collect(Collectors.toList());
                }

                // Lấy tên các tiện ích của khách sạn
                List<String> facilityNames = hotel.getFacilities().stream()
                        .map(HotelFacility::getName)  // Lấy tên tiện ích
                        .collect(Collectors.toList());

                // Tạo đối tượng phản hồi với tất cả thông tin cần thiết
                HotelResponse hotelResponse = new HotelResponse(
                        hotel.getId(),
                        hotel.getName(),
                        hotel.getDescription(),
                        encodePhoto(hotel.getCoverPhoto()),  // Ảnh đại diện
                        encodedPhotos,  // Danh sách ảnh phụ
                        hotel.getStatus(),
                        hotel.getEmail(),
                        hotel.getPhoneNumber(),
                        hotel.getStreet(),
                        hotel.getWard() != null ? hotel.getWard().getName() : "N/A",  // Tên phường
                        hotel.getWard() != null && hotel.getWard().getDistrict() != null ? hotel.getWard().getDistrict().getName() : "N/A",  // Tên quận
                        hotel.getWard() != null && hotel.getWard().getDistrict() != null && hotel.getWard().getDistrict().getProvince() != null ? hotel.getWard().getDistrict().getProvince().getName() : "N/A",  // Tên tỉnh
                        facilityNames // Danh sách tiện ích
                );

                hotelResponses.add(hotelResponse);
            }

            // Trả về danh sách khách sạn kèm thông tin chi tiết
            return ResponseEntity.ok(hotelResponses);

        } catch (Exception e) {
            HotelResponse errorResponse = new HotelResponse();
            errorResponse.setName("Error retrieving pending hotels");
            errorResponse.setDescription("An error occurred while retrieving the pending hotels list.");
            errorResponse.setStatus("ERROR");
            errorResponse.setEmail(e.getMessage());  // Có thể lưu thông tin lỗi chi tiết tại đây

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonList(errorResponse));
        }
    }



    // API phê duyệt khách sạn
    @PutMapping("/approve/{hotelId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> approveHotel(@PathVariable Long hotelId) {
        Hotel approvedHotel = hotelService.approveHotel(hotelId);
        return ResponseEntity.ok("Hotel approved successfully: " + approvedHotel.getName());
    }


    // Lấy danh sách các Hotel của chủ sở hữu
    @GetMapping("/my-hotels")
    @PreAuthorize("hasRole('ROLE_RENTAL')")
    public ResponseEntity<List<HotelResponse>> getMyHotels(@AuthenticationPrincipal UserDetails userDetails) {
        String ownerEmail = userDetails.getUsername(); // Email lấy từ token
        List<Hotel> hotels = hotelService.getHotelsByOwner(ownerEmail); // Lấy khách sạn của người dùng

        List<HotelResponse> hotelResponses = new ArrayList<>();

        for (Hotel hotel : hotels) {
            byte[] photoBytes = null;
            Blob photoBlob = hotel.getCoverPhoto();  // Giả sử hotel có trường photo kiểu Blob
            if (photoBlob != null) {
                try {
                    photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
                } catch (SQLException e) {
                    throw new PhotoRetrievalException("Error retrieving photo for hotel with ID " + hotel.getId());
                }
            }

            // Chuyển đổi byte[] thành Base64 nếu có ảnh
            String base64Photo = photoBytes != null ? Base64.encodeBase64String(photoBytes) : null;

            HotelResponse hotelResponse = new HotelResponse(
                    hotel.getId(),
                    hotel.getName(),
                    hotel.getDescription(),
                    base64Photo,  // Trả về ảnh dưới dạng Base64
                    hotel.getStatus(),
                    hotel.getEmail(),
                    hotel.getPhoneNumber(),
                    hotel.getStreet(),
                    hotel.getWard() != null ? hotel.getWard().getName() : "N/A", // Phường
                    hotel.getWard() != null && hotel.getWard().getDistrict() != null ? hotel.getWard().getDistrict().getName() : "N/A", // Quận
                    hotel.getWard() != null && hotel.getWard().getDistrict() != null && hotel.getWard().getDistrict().getProvince() != null ? hotel.getWard().getDistrict().getProvince().getName() : "N/A" // Tỉnh
            );
            hotelResponses.add(hotelResponse);
        }

        // Trả về danh sách khách sạn kèm ảnh (nếu có)
        return ResponseEntity.ok(hotelResponses.isEmpty() ? Collections.emptyList() : hotelResponses);
    }
    // Admin từ chối Hotel

    @PutMapping("/reject/{hotelId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Hotel> rejectHotel(@PathVariable Long hotelId) {
        Hotel rejectedHotel = hotelService.rejectHotel(hotelId);
        return ResponseEntity.ok(rejectedHotel);
    }

    // Lấy tất cả các Hotel
    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<Hotel>> getAllHotels() {
        List<Hotel> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(hotels);
    }

    // Xóa Hotel
    @DeleteMapping("/delete/{hotelId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long hotelId) {
        hotelService.deleteHotel(hotelId);
        return ResponseEntity.noContent().build();
    }

    // Thêm Room vào Hotel
    @PostMapping("/{hotelId}/rooms")
    @PreAuthorize("hasRole('ROLE_RENTAL')")
    public ResponseEntity<Hotel> addRoomToHotel(@PathVariable Long hotelId, @RequestBody Room room) {
        Hotel updatedHotel = hotelService.addRoomToHotel(hotelId, room);
        return ResponseEntity.ok(updatedHotel);
    }
//    @GetMapping("/by-ward/{wardId}")
//    public List<Hotel> getHotelsByWard(@PathVariable Long wardId) {
//        return hotelService.findHotelsByWard(wardId);
//    }
//
//    @GetMapping("/by-district/{districtId}")
//    public List<Hotel> getHotelsByDistrict(@PathVariable Long districtId) {
//        return hotelService.findHotelsByDistrict(districtId);
//    }
//
//    @GetMapping("/by-province/{provinceId}")
//    public List<Hotel> getHotelsByProvince(@PathVariable Long provinceId) {
//        return hotelService.findHotelsByProvince(provinceId);
//    }
    @GetMapping("/by-ward/{wardId}")
    public ResponseEntity<List<HotelResponse>> getHotelsByWard(@PathVariable Long wardId) {
    return ResponseEntity.ok(convertHotelsToResponses(hotelService.findHotelsByWard(wardId)));
}

    @GetMapping("/by-district/{districtId}")
    public ResponseEntity<List<HotelResponse>> getHotelsByDistrict(@PathVariable Long districtId) {
        return ResponseEntity.ok(convertHotelsToResponses(hotelService.findHotelsByDistrict(districtId)));
    }

    @GetMapping("/by-province/{provinceId}")
    public ResponseEntity<List<HotelResponse>> getHotelsByProvince(@PathVariable Long provinceId) {
        return ResponseEntity.ok(convertHotelsToResponses(hotelService.findHotelsByProvince(provinceId)));
    }


    private List<HotelResponse> convertHotelsToResponses(List<Hotel> hotels) {
        List<HotelResponse> hotelResponses = new ArrayList<>();

        for (Hotel hotel : hotels) {
            // Kiểm tra trạng thái của khách sạn trước khi xử lý
            if ("APPROVED".equals(hotel.getStatus())) {
                String coverPhotoBase64 = encodePhoto(hotel.getCoverPhoto());

                // Chuyển đổi danh sách ảnh phụ sang Base64
                List<String> photoBase64List = new ArrayList<>();
                for (HotelPhoto photo : hotel.getPhotos()) {
                    String photoBase64 = encodePhoto(photo.getPhoto());
                    if (photoBase64 != null) {
                        photoBase64List.add(photoBase64);
                    }
                }

                // Chuyển đổi danh sách facilities thành chuỗi tên
                List<String> facilities = hotel.getFacilities().stream()
                        .map(HotelFacility::getName)
                        .toList();

                // Tìm giá phòng rẻ nhất
                BigDecimal cheapestRoomPrice = hotel.getRooms().stream()
                        .map(Room::getRoomPrice)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

                HotelResponse hotelResponse = new HotelResponse(
                        hotel.getId(),
                        hotel.getName(),
                        hotel.getDescription(),
                        coverPhotoBase64, // Ảnh đại diện
                        photoBase64List,
                        hotel.getStatus(),
                        hotel.getEmail(),
                        hotel.getPhoneNumber(),
                        hotel.getStreet(),
                        hotel.getWard() != null ? hotel.getWard().getName() : "N/A",
                        hotel.getWard() != null && hotel.getWard().getDistrict() != null ? hotel.getWard().getDistrict().getName() : "N/A",
                        hotel.getWard() != null && hotel.getWard().getDistrict().getProvince() != null ? hotel.getWard().getDistrict().getProvince().getName() : "N/A",
                        facilities,
                        hotel.getRatingCount(),
                        hotel.getStarRating(),
                        cheapestRoomPrice
                );

                hotelResponses.add(hotelResponse);
            }
        }

        return hotelResponses;
    }

    @GetMapping("/hotels/{id}")
    public ResponseEntity<?> getHotelById(@PathVariable Long id) {
        try {
            Optional<Hotel> hotelOptional = hotelService.getHotelById(id);
            if (hotelOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hotel not found");
            }

            Hotel hotel = hotelOptional.get();
            List<String> encodedPhotos = new ArrayList<>();

            // Encode photos to Base64
            if (hotel.getPhotos() != null && !hotel.getPhotos().isEmpty()) {
                encodedPhotos = hotel.getPhotos().stream()
                        .filter(photo -> photo.getPhoto() != null) // Bỏ qua ảnh null
                        .map(photo -> encodePhoto(photo.getPhoto()))
                        .collect(Collectors.toList());
            }

            // Lấy tên các tiện ích của khách sạn
            List<String> facilityNames = hotel.getFacilities().stream()
                    .map(HotelFacility::getName)  // Lấy tên tiện ích
                    .collect(Collectors.toList());

            // Tạo đối tượng phản hồi với tất cả thông tin cần thiết
            HotelResponse hotelResponse = new HotelResponse(
                    hotel.getId(),
                    hotel.getName(),
                    hotel.getDescription(),
                    encodePhoto(hotel.getCoverPhoto()),  // Ảnh đại diện
                    encodedPhotos,  // Danh sách ảnh phụ
                    hotel.getStatus(),
                    hotel.getEmail(),
                    hotel.getPhoneNumber(),
                    hotel.getStreet(),
                    hotel.getWard() != null ? hotel.getWard().getName() : "N/A",
                    hotel.getWard() != null && hotel.getWard().getDistrict() != null ? hotel.getWard().getDistrict().getName() : "N/A",
                    hotel.getWard() != null && hotel.getWard().getDistrict() != null && hotel.getWard().getDistrict().getProvince() != null ? hotel.getWard().getDistrict().getProvince().getName() : "N/A",
                    facilityNames // Thêm tên các tiện ích vào phản hồi
            );

            return ResponseEntity.ok(hotelResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving hotel: " + e.getMessage());
        }
    }


    // Phương thức mã hóa ảnh (không thay đổi)
    private String encodePhoto(Blob photoBlob) {
        if (photoBlob != null) {
            try {
                byte[] photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
                return Base64.encodeBase64String(photoBytes);
            } catch (SQLException e) {
                throw new RuntimeException("Error retrieving photo", e);
            }
        }
        return null;
    }

    @GetMapping("/address/{hotelId}")
    public ResponseEntity<String> getHotelAddress(@PathVariable Long hotelId) {
        try {
            String address = hotelService.getAddressByHotelId(hotelId);
            return ResponseEntity.ok(address);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching the address.");
        }
    }

}
