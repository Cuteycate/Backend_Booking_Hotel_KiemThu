package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.Hotel;
import com.example.hotel_booking_be_v1.model.HotelDTO;
import com.example.hotel_booking_be_v1.model.Room;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

    public interface IHotelService {
        Hotel addHotel(HotelDTO hotelDTO, String ownerEmail) throws IOException, SQLException;
        Hotel updateHotel(Long hotelId, HotelDTO hotelDTO) throws Exception;
        Hotel getHotelById1(Long hotelId);
     Optional<Hotel> getHotelById(Long hotelId) throws Exception;

    List<Hotel> getHotelsByOwner(String ownerEmail);

    Hotel approveHotel(Long hotelId);

    Hotel rejectHotel(Long hotelId);

    List<Hotel> getAllHotels();

    void deleteHotel(Long hotelId);

    Hotel addRoomToHotel(Long hotelId, Room room);

    List<Hotel> getHotelsByStatus(String status);

    public List<Hotel> findHotelsByWard(Long wardId);

    // Tìm khách sạn theo quận/huyện
    public List<Hotel> findHotelsByDistrict(Long districtId);

    // Tìm khách sạn theo tỉnh/thành
    public List<Hotel> findHotelsByProvince(Long provinceId);

    String getAddressByHotelId(Long hotelId);

        List<Hotel> findHotelsByLocation(Long locationCode, String locationType);
    }
