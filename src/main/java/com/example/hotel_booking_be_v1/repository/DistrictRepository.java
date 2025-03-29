package com.example.hotel_booking_be_v1.repository;

import com.example.hotel_booking_be_v1.model.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    // Tìm quận/huyện theo tên
    List<District> findByNameContaining(String name);

    // Tìm tất cả quận/huyện thuộc một tỉnh/thành
    List<District> findByProvinceId(Long provinceId);

    List<District> findByNameContainingIgnoreCase(String name);

}