package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.HotelFacility;
import com.example.hotel_booking_be_v1.repository.HotelFacilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelFacilityService {
    private final HotelFacilityRepository hotelFacilityRepository;

    @Autowired
    public HotelFacilityService(HotelFacilityRepository hotelFacilityRepository) {
        this.hotelFacilityRepository = hotelFacilityRepository;
    }

    public HotelFacility findById(Long id) {
        return hotelFacilityRepository.findById(id).orElse(null);
    }

    public List<HotelFacility> getAllFacilities() {
        return hotelFacilityRepository.findAll();

    }

    // Add a new facility
    public HotelFacility addFacility(HotelFacility facility) {
        return hotelFacilityRepository.save(facility);
    }
}
