package com.example.hotel_booking_be_v1.repository;

import com.example.hotel_booking_be_v1.model.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {
    // Tìm tỉnh/thành phố theo tên
    List<Province> findByNameContaining(String name);
    List<Province> findByNameContainingIgnoreCase(String name);
}