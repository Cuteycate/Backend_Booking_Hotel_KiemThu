package com.example.hotel_booking_be_v1.repository;

import com.example.hotel_booking_be_v1.model.RoomPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomPhotoRepository extends JpaRepository<RoomPhoto, Long> {

    // Tìm tất cả ảnh của phòng theo phòng (room)
    List<RoomPhoto> findByRoomId(Long roomId);

    // Xóa ảnh phòng theo phòng (room)
    void deleteByRoomId(Long roomId);

    // Tìm ảnh phòng theo id
    Optional<RoomPhoto> findById(Long id);

}
