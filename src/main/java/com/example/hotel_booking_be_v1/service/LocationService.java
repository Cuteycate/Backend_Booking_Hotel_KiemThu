package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.repository.DistrictRepository;
import com.example.hotel_booking_be_v1.repository.ProvinceRepository;
import com.example.hotel_booking_be_v1.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocationService {

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;

    @Autowired
    public LocationService(ProvinceRepository provinceRepository, DistrictRepository districtRepository, WardRepository wardRepository) {
        this.provinceRepository = provinceRepository;
        this.districtRepository = districtRepository;
        this.wardRepository = wardRepository;
    }

    public List<Map<String, String>> searchLocations(String query) {
        List<Map<String, String>> locations = new ArrayList<>();

        // Tìm kiếm tỉnh/thành
        provinceRepository.findByNameContainingIgnoreCase(query).forEach(province -> {
            Map<String, String> result = new HashMap<>();
            result.put("type", "Province");
            result.put("name", province.getName());
            result.put("id", String.valueOf(province.getId()));
            locations.add(result);
        });

        // Tìm kiếm quận/huyện
        districtRepository.findByNameContainingIgnoreCase(query).forEach(district -> {
            Map<String, String> result = new HashMap<>();
            result.put("type", "District");
            result.put("name", district.getName());
            result.put("id", String.valueOf(district.getId()));
            locations.add(result);
        });

        // Tìm kiếm xã/phường
        wardRepository.findByNameContainingIgnoreCase(query).forEach(ward -> {
            Map<String, String> result = new HashMap<>();
            result.put("type", "Ward");
            result.put("name", ward.getName());
            result.put("id", String.valueOf(ward.getId()));
            locations.add(result);
        });

        return locations;
    }
}