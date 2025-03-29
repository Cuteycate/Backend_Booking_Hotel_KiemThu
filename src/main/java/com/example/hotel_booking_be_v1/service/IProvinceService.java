package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.Province;

import java.util.List;

public interface IProvinceService {
    public List<Province> findAllProvinces();

    public List<Province> searchProvincesByName(String name);
}