package com.example.hotel_booking_be_v1.repository;

import com.example.hotel_booking_be_v1.model.RoomFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomFacilityRepository extends JpaRepository<RoomFacility, Long> {
}
