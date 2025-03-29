package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.model.HotelFacility;
import com.example.hotel_booking_be_v1.model.RoomFacility;
import com.example.hotel_booking_be_v1.model.RoomFacilityDTO;
import com.example.hotel_booking_be_v1.service.RoomFacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roomFacilities")
public class RoomFacilityController {

    @Autowired
    private RoomFacilityService roomfacilityService;

    @PostMapping
    public ResponseEntity<RoomFacility> createFacility(@ModelAttribute RoomFacilityDTO facilityDTO) {
        RoomFacility facility = new RoomFacility();
        facility.setName(facilityDTO.getName());
        RoomFacility newFacility = roomfacilityService.addFacility(facility);
        return ResponseEntity.ok(newFacility);
    }

    @GetMapping
    public ResponseEntity<List<RoomFacility>> getAllFacilities() {
        List<RoomFacility> facilities = roomfacilityService.getAllFacilities();
        return ResponseEntity.ok(facilities);
    }
}
