package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.RoomType;

import java.util.List;

public interface IRoomTypeService {

    public List<RoomType> findAllRoomType();

    public RoomType findRoomTypeById(Long roomTypeId);

    public void addNewRoomType(RoomType roomType);


    List<RoomType> getRoomTypes();
}
