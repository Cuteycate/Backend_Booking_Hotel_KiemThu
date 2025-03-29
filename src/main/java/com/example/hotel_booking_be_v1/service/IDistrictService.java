package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.District;

import java.util.List;

public interface IDistrictService {

    public List<District> findAllDistricts();

    public List<District> findDistrictsByProvince(Long provinceId);

    public List<District> searchDistrictsByName(String name);
}