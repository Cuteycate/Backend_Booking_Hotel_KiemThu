package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.Utils.ImageUtils;
import com.example.hotel_booking_be_v1.exception.ResourceNotFoundException;
import com.example.hotel_booking_be_v1.model.*;
import com.example.hotel_booking_be_v1.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService implements IHotelService {
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final WardRepository wardRepository;
    private final HotelPhotoRepository hotelPhotoRepository;
    private final HotelFacilityService hotelFacilityService;
    private final HotelFacilityRepository hotelFacilityRepository;

    @Override
    public Hotel addHotel(HotelDTO hotelDTO, String ownerEmail) throws IOException, SQLException {
        // Tìm User (owner)
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        // Tìm Ward
        Ward ward = wardRepository.findById(hotelDTO.getWardId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        // Tạo đối tượng Hotel
        Hotel hotel = new Hotel();
        hotel.setName(hotelDTO.getName());
        hotel.setPhoneNumber(hotelDTO.getPhoneNumber());
        hotel.setEmail(hotelDTO.getEmail());
        hotel.setDescription(hotelDTO.getDescription());
        hotel.setStreet(hotelDTO.getStreet());
        hotel.setWard(ward);
        hotel.setOwner(owner);
        hotel.setStatus("PENDING");

        // Lưu ảnh đại diện (cover photo)
        hotel.setCoverPhoto(new SerialBlob(hotelDTO.getCoverPhoto().getBytes()));

        List<HotelFacility> facilities = new ArrayList<>();
        if (hotelDTO.getFacilityIds() != null && !hotelDTO.getFacilityIds().isEmpty()) {
            for (Long facilityId : hotelDTO.getFacilityIds()) {
                HotelFacility facility = hotelFacilityService.findById(facilityId);
                if (facility != null) {
                    facilities.add(facility);
                }
            }
        }
        hotel.setFacilities(facilities);

        // Lưu Hotel trước để có ID
        Hotel savedHotel = hotelRepository.save(hotel);

        // Lưu danh sách ảnh khác (nếu có)
        if (hotelDTO.getPhotos() != null && !hotelDTO.getPhotos().isEmpty()) {
            for (MultipartFile photo : hotelDTO.getPhotos()) {
                HotelPhoto hotelPhoto = new HotelPhoto();
                hotelPhoto.setPhoto(new SerialBlob(photo.getBytes()));
                hotelPhoto.setHotel(savedHotel);
                hotelPhotoRepository.save(hotelPhoto);
            }
        }
        return savedHotel;
    }
    @Override
    @Transactional
    public Hotel updateHotel(Long hotelId, HotelDTO hotelDTO) throws Exception {
        // Tìm khách sạn theo ID
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new Exception("Hotel not found"));

        // Cập nhật thông tin cơ bản từ DTO
        if (hotelDTO.getName() != null) {
            hotel.setName(hotelDTO.getName());
        }
        if (hotelDTO.getPhoneNumber() != null) {
            hotel.setPhoneNumber(hotelDTO.getPhoneNumber());
        }
        if (hotelDTO.getEmail() != null) {
            hotel.setEmail(hotelDTO.getEmail());
        }
        if (hotelDTO.getDescription() != null) {
            hotel.setDescription(hotelDTO.getDescription());
        }
        if (hotelDTO.getStreet() != null) {
            hotel.setStreet(hotelDTO.getStreet());
        }

        if (hotelDTO.getFacilityNames() != null) {
            // Xóa các facility cũ
            hotelFacilityRepository.deleteByHotelId(hotel.getId());

            // Thêm các facility mới
            List<HotelFacility> newFacilities = hotelDTO.getFacilityNames().stream()
                    .map(name -> {
                        HotelFacility facility = hotelFacilityRepository.findByName(name)
                                .orElseThrow(() -> new RuntimeException("Facility not found with name: " + name));
                        return facility;
                    }).collect(Collectors.toList());

            hotel.setFacilities(newFacilities);
        }

        // Cập nhật Ward (nếu có)
        if (hotelDTO.getWardId() != null) {
            Ward ward = wardRepository.findById(hotelDTO.getWardId())
                    .orElseThrow(() -> new Exception("Ward not found"));
            hotel.setWard(ward);
        }

        // Cập nhật ảnh đại diện (cover photo) mới
        if (hotelDTO.getCoverPhoto() != null) {
            Blob coverPhotoBlob = convertMultipartFileToBlob(hotelDTO.getCoverPhoto());
            hotel.setCoverPhoto(coverPhotoBlob);
        }

        // Cập nhật hoặc xóa các ảnh cũ không còn cần thiết
        if (hotelDTO.getPhotos() != null && !hotelDTO.getPhotos().isEmpty()) {
            hotel.getPhotos().clear();

            List<HotelPhoto> newHotelPhotos = new ArrayList<>();
            for (MultipartFile photo : hotelDTO.getPhotos()) {
                Blob photoBlob = convertMultipartFileToBlob(photo);

                HotelPhoto hotelPhoto = new HotelPhoto();
                hotelPhoto.setPhoto(photoBlob);
                hotelPhoto.setHotel(hotel);
                newHotelPhotos.add(hotelPhoto);
            }
            hotel.getPhotos().addAll(newHotelPhotos);
        }

        try {
            hotelRepository.save(hotel);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating hotel: " + e.getMessage());
        }
        return hotel;
    }

    // Helper method to convert MultipartFile to Blob
    private Blob convertMultipartFileToBlob(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return new javax.sql.rowset.serial.SerialBlob(inputStream.readAllBytes());
        } catch (SQLException e) {
            throw new IOException("Failed to convert file to Blob", e);
        }
    }


    private byte[] getBytesFromBlob(Blob blob) throws SQLException, IOException {
        InputStream inputStream = blob.getBinaryStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }


    @Override
    public List<Hotel> getHotelsByStatus(String status) {
        // Kiểm tra giá trị trạng thái
        if (!List.of("PENDING", "REJECTED").contains(status.toUpperCase())) {
            throw new IllegalArgumentException("Invalid hotel status: " + status);
        }
        return hotelRepository.findByStatus(status.toUpperCase());
    }

    @Override
    public Optional<Hotel> getHotelById(Long hotelId) throws Exception {
        return hotelRepository.findById(hotelId);
    }

    public Hotel getHotelById1(Long hotelId) {
        return hotelRepository.findById(hotelId).orElse(null);
    }

    @Override
    public List<Hotel> getHotelsByOwner(String ownerEmail) {
        // Tìm người dùng theo email
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Kiểm tra xem người dùng có vai trò 'ROLE_RENTAL' không
        boolean isRental = owner.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_RENTAL"));

        if (!isRental) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a rental");
        }

        // Lấy danh sách khách sạn của người sở hữu
        return hotelRepository.findAllByOwnerId(owner.getId());
    }
    @Override
    public Hotel approveHotel(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
        if (!"PENDING".equalsIgnoreCase(hotel.getStatus())) {
            throw new IllegalStateException("Only pending hotels can be approved");
        }
        hotel.setStatus("APPROVED");
        return hotelRepository.save(hotel);
    }


    @Override
    public Hotel rejectHotel(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if ("REJECTED".equalsIgnoreCase(hotel.getStatus())) {
            throw new IllegalStateException("Hotel is already rejected");
        }

        hotel.setStatus("REJECTED");
        return hotelRepository.save(hotel);
    }

    @Override
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    @Override
    public void deleteHotel(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
        hotelRepository.delete(hotel);
    }

    @Override
    public Hotel addRoomToHotel(Long hotelId, Room room) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if (!hotel.getStatus().equalsIgnoreCase("APPROVED")) {
            throw new IllegalStateException("Cannot add room to a hotel that is not approved");
        }

        room.setHotel(hotel);
        hotel.getRooms().add(room);

        roomRepository.save(room);
        return hotelRepository.save(hotel);
    }
    // Tìm khách sạn theo xã/phường
    public List<Hotel> findHotelsByWard(Long wardId) {
        return hotelRepository.findByWardId(wardId);
    }

    // Tìm khách sạn theo quận/huyện
    public List<Hotel> findHotelsByDistrict(Long districtId) {
        return hotelRepository.findByDistrictId(districtId);
    }

    // Tìm khách sạn theo tỉnh/thành
    public List<Hotel> findHotelsByProvince(Long provinceId) {
        return hotelRepository.findByProvinceId(provinceId);
    }

    @Override
    public String getAddressByHotelId(Long hotelId) {
        Hotel hotel = hotelRepository.findHotelWithAddress(hotelId);
        if (hotel == null) {
            throw new EntityNotFoundException("Hotel not found with ID: " + hotelId);
        }

        // Lấy thông tin địa chỉ
        Ward ward = hotel.getWard();
        District district = ward.getDistrict();
        Province province = district.getProvince();

        return String.format(
                "%s, %s, %s, %s",
                hotel.getStreet(),
                ward.getName(),
                district.getName(),
                province.getName()
        );
    }

    @Override
    public List<Hotel> findHotelsByLocation(Long locationCode, String locationType) {
        List<Hotel> hotels;
        switch (locationType.toLowerCase()) {
            case "ward":
                hotels = hotelRepository.findByWardCode(locationCode);
                break;
            case "district":
                hotels = hotelRepository.findByDistrictCode(locationCode);
                break;
            case "province":
                hotels = hotelRepository.findByProvinceCode(locationCode);
                break;
            default:
                throw new IllegalArgumentException("Invalid location type. Must be 'ward', 'district', or 'province'.");
        }
        return hotels;
    }
}
