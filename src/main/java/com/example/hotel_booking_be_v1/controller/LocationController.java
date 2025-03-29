package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.model.District;
import com.example.hotel_booking_be_v1.model.Province;
import com.example.hotel_booking_be_v1.model.Ward;
import com.example.hotel_booking_be_v1.service.DistrictService;
import com.example.hotel_booking_be_v1.service.ProvinceService;
import com.example.hotel_booking_be_v1.service.WardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")  // Đường dẫn API đã sửa thành /api/locations
public class LocationController {

    private final ProvinceService provinceService;
    private final DistrictService districtService;
    private final WardService wardService;

    @Autowired
    public LocationController(ProvinceService provinceService, DistrictService districtService, WardService wardService) {
        this.provinceService = provinceService;
        this.districtService = districtService;
        this.wardService = wardService;
    }

    // API lấy tất cả tỉnh/thành phố
    @PreAuthorize("hasAnyRole('ROLE_RENTAL', 'ROLE_ADMIN')")
    @GetMapping("/provinces")
    public List<Province> getAllProvinces() {
        return provinceService.findAllProvinces();
    }

    // API tìm tỉnh theo tên
    @GetMapping("/provinces/search")
    public List<Province> searchProvinces(@RequestParam String name) {
        return provinceService.searchProvincesByName(name);
    }

    // API lấy tất cả quận/huyện của tỉnh
    @GetMapping("/districts")
    public List<District> getDistrictsByProvince(@RequestParam Long provinceId) {
        return districtService.findDistrictsByProvince(provinceId);
    }

    // API tìm quận/huyện theo tên
    @GetMapping("/districts/search")
    public List<District> searchDistricts(@RequestParam String name) {
        return districtService.searchDistrictsByName(name);
    }

    // API lấy tất cả xã/phường của quận
    @GetMapping("/wards")
    public List<Ward> getWardsByDistrict(@RequestParam Long districtId) {
        return wardService.findWardsByDistrict(districtId);
    }

    // API tìm xã/phường theo tên
    @GetMapping("/wards/search")
    public List<Ward> searchWards(@RequestParam String name) {
        return wardService.searchWardsByName(name);
    }
}
