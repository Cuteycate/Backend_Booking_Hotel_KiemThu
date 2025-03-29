package com.example.hotel_booking_be_v1.repository;

import com.example.hotel_booking_be_v1.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByStatus(String status); // Tìm theo trạng thái (PENDING, APPROVED, REJECTED)
    List<Hotel> findByOwner_Id(Long ownerId); // Tìm khách sạn của một renter
    List<Hotel> findAllByOwnerId(Long ownerId);
    // Tìm khách sạn theo xã/phường
    List<Hotel> findByWardId(Long wardId);

    // Tìm khách sạn theo quận/huyện thông qua quan hệ ward
    @Query("SELECT h FROM Hotel h WHERE h.ward.district.id = :districtId")
    List<Hotel> findByDistrictId(@Param("districtId") Long districtId);

    // Tìm khách sạn theo tỉnh/thành thông qua quan hệ ward → district → province
    @Query("SELECT h FROM Hotel h WHERE h.ward.district.province.id = :provinceId")
    List<Hotel> findByProvinceId(@Param("provinceId") Long provinceId);

    @Query("SELECT h FROM Hotel h JOIN FETCH h.ward w JOIN FETCH w.district d JOIN FETCH d.province p WHERE h.id = :hotelId")
    Hotel findHotelWithAddress(@Param("hotelId") Long hotelId);

    @Query("SELECT h FROM Hotel h WHERE h.ward.code = :wardCode")
    List<Hotel> findByWardCode(@Param("wardCode") Long wardCode);

    // Tìm khách sạn theo mã quận/huyện
    @Query("SELECT h FROM Hotel h WHERE h.ward.district.code = :districtCode")
    List<Hotel> findByDistrictCode(@Param("districtCode") Long districtCode);

    // Tìm khách sạn theo mã tỉnh/thành
    @Query("SELECT h FROM Hotel h WHERE h.ward.district.province.code = :provinceCode")
    List<Hotel> findByProvinceCode(@Param("provinceCode") Long provinceCode);

    List<Hotel> findByNameContaining(String name);
    @Query("SELECT h.id FROM Hotel h WHERE h.name = :hotelName")
    Optional<Long> findHotelIdByName(@Param("hotelName") String hotelName);
}
