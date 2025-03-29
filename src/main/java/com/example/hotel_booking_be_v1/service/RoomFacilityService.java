package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.HotelFacility;
import com.example.hotel_booking_be_v1.model.RoomFacility;
import com.example.hotel_booking_be_v1.repository.RoomFacilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomFacilityService {
    private final RoomFacilityRepository roomFacilityRepository;

    @Autowired
    public RoomFacilityService(RoomFacilityRepository roomFacilityRepository){
        this.roomFacilityRepository = roomFacilityRepository;
    }

    public RoomFacility findById (Long id){
        return roomFacilityRepository.findById(id).orElse(null);
    }

    public List<RoomFacility> findByIds(List<Long> ids) {
        return roomFacilityRepository.findAllById(ids);
    }

    public List<RoomFacility> getAllFacilities() {
        return roomFacilityRepository.findAll();
    }
    // Add a new facility
    public RoomFacility addFacility(RoomFacility facility) {
        return roomFacilityRepository.save(facility);
    }
}