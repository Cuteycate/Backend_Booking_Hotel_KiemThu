package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.Ward;

import java.util.List;

public interface IWardService {
    public List<Ward> findAllWards();

    public List<Ward> findWardsByDistrict(Long districtId);

    public List<Ward> searchWardsByName(String name);
}