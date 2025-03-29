package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.model.HotelFacility;
import com.example.hotel_booking_be_v1.model.HotelFacilityDTO;
import com.example.hotel_booking_be_v1.service.HotelFacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
public class HotelFacilityController {
    @Autowired
    private HotelFacilityService hotelfacilityService;

    @PostMapping
    public ResponseEntity<HotelFacility> createFacility(@ModelAttribute HotelFacilityDTO facilityDTO) {
        HotelFacility facility = new HotelFacility();
        facility.setName(facilityDTO.getName());
        HotelFacility newFacility = hotelfacilityService.addFacility(facility);
        return ResponseEntity.ok(newFacility);
    }

    // Get all facilities

    @GetMapping
    public ResponseEntity<List<HotelFacility>> getAllFacilities() {
        List<HotelFacility> facilities = hotelfacilityService.getAllFacilities();
        return ResponseEntity.ok(facilities);
    }
}
