package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.RoomType;
import com.example.hotel_booking_be_v1.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomTypeService implements IRoomTypeService{
    private final RoomTypeRepository roomTypeRepository;

    @Autowired
    public RoomTypeService(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    public List<RoomType> findAllRoomType() {
        return roomTypeRepository.findAll();
    }

    @Override
    public RoomType findRoomTypeById(Long id) {
        return roomTypeRepository.findById(id).orElse(null);
    }

    public void addNewRoomType(RoomType roomType) {
        roomTypeRepository.save(roomType);
    }

    @Override
    public List<RoomType> getRoomTypes() {
        return roomTypeRepository.findAll();
    }

}
