package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.Room;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IRoomService {

//    List<Room> getRoomsByHotel(Long hotelId);
    List<Room> getRoomsByHotelsId(Long hotelId);
    void addNewRoom(Room room);
    List<Room> getAllRoomByIds(List<Long> ids);
}
