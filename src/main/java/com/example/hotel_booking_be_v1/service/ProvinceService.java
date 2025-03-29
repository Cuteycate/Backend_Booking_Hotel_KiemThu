package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.Province;
import com.example.hotel_booking_be_v1.repository.ProvinceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProvinceService implements IProvinceService {
    private final ProvinceRepository provinceRepository;

    @Autowired
    public ProvinceService(ProvinceRepository provinceRepository) {
        this.provinceRepository = provinceRepository;
    }

    public List<Province> findAllProvinces() {
        return provinceRepository.findAll();
    }

    public List<Province> searchProvincesByName(String name) {
        return provinceRepository.findByNameContaining(name);
    }
}