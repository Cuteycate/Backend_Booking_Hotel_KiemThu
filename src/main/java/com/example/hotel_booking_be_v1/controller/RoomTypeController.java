package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.model.RoomType;
import com.example.hotel_booking_be_v1.service.IRoomTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room/type")
public class RoomTypeController {
    private final IRoomTypeService roomTypeService;
    public RoomTypeController(IRoomTypeService roomTypeService){
        this.roomTypeService = roomTypeService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addRoomType(@RequestBody RoomType roomType) {
        try {
            RoomType newRoomType = new RoomType();
            newRoomType.setName(roomType.getName());
            roomTypeService.addNewRoomType(newRoomType);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Hotel room type successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while adding room type: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<RoomType>> getRoomTypes() {
        List<RoomType> roomTypes = roomTypeService.getRoomTypes();
        return ResponseEntity.ok(roomTypes);
    }
}