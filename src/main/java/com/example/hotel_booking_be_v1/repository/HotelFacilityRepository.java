package com.example.hotel_booking_be_v1.repository;

import com.example.hotel_booking_be_v1.model.Hotel;
import com.example.hotel_booking_be_v1.model.HotelFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelFacilityRepository extends JpaRepository<HotelFacility, Long> {
    Optional<HotelFacility> findByName(String name);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM hotel_facility_mapping WHERE hotel_id = :hotelId", nativeQuery = true)
    void deleteByHotelId(@Param("hotelId") Long hotelId);
}
