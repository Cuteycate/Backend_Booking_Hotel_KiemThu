package com.example.hotel_booking_be_v1.repository;

import com.example.hotel_booking_be_v1.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room,Long> {
    List<Room> findAllByHotelId(Long hotelId);

    List<Room> findByHotelId(Long hotelId);

}
