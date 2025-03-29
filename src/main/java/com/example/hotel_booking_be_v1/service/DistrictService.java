package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.District;
import com.example.hotel_booking_be_v1.repository.DistrictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistrictService implements IDistrictService{
    private final DistrictRepository districtRepository;

    @Autowired
    public DistrictService(DistrictRepository districtRepository) {
        this.districtRepository = districtRepository;
    }

    public List<District> findAllDistricts() {
        return districtRepository.findAll();
    }

    public List<District> findDistrictsByProvince(Long provinceId) {
        return districtRepository.findByProvinceId(provinceId);
    }

    public List<District> searchDistrictsByName(String name) {
        return districtRepository.findByNameContaining(name);
    }
}