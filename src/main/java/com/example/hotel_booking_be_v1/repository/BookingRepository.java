package com.example.hotel_booking_be_v1.repository;

import com.example.hotel_booking_be_v1.model.Booking;
import com.example.hotel_booking_be_v1.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    List<Booking> findByUserId(Long userId); // Tìm đặt phòng theo người dùng
    List<Booking> findByHotelId(Long hotelId); // Tìm đặt phòng theo khách sạn
    List<Booking> findByStatus(BookingStatus status); // Tìm đặt phòng theo trạng thái
    List<Booking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);
    @Query("SELECT COUNT(r.id) " +
            "FROM Booking b " +
            "JOIN b.rooms r " +
            "WHERE r.id = :roomId " +
            "AND ((:checkInDate BETWEEN b.checkInDate AND b.checkOutDate) " +
            "OR (:checkOutDate BETWEEN b.checkInDate AND b.checkOutDate) " +
            "OR (b.checkInDate BETWEEN :checkInDate AND :checkOutDate))")
    int countBookedRooms(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Booking b " +
            "WHERE b.id = :bookingId " +
            "AND b.user.id = :userId " +
            "AND b.status = :status " +
            "AND b.checkOutDate < :currentDate")
    boolean existsByBookingIdAndUserIdAndStatusAndCheckOutDateBefore(
            @Param("bookingId") Long bookingId,
            @Param("userId") Long userId,
            @Param("status") BookingStatus status, // Thay String bằng BookingStatus
            @Param("currentDate") LocalDate currentDate
    );


    @Query("SELECT b FROM Booking b WHERE b.hotel.id = :hotelId " +
            "AND ((b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate))")
    List<Booking> findOverlappingBookings(@Param("hotelId") Long hotelId,
                                          @Param("checkInDate") LocalDate checkInDate,
                                          @Param("checkOutDate") LocalDate checkOutDate);

    @Query("SELECT b FROM Booking b JOIN b.rooms r WHERE r.id = :roomId AND b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate")
    List<Booking> findBookingsByRoomAndDateRange(@Param("roomId") Long roomId, @Param("checkInDate") LocalDate checkInDate, @Param("checkOutDate") LocalDate checkOutDate);

    @Query("""
       SELECT b.id AS bookingId, 
              b.checkInDate AS checkInDate, 
              b.checkOutDate AS checkOutDate, 
              b.status AS status, 
              u.email AS email, 
              u.firstName AS name,
              h.name AS hotelName, 
              r.name AS roomName,
              i.totalPrice AS totalPrice,
              i.depositAmount AS depositAmount,
              i.paymentDate AS paymentDate,
              i.paymentMethod AS paymentMethod
       FROM Booking b
       JOIN b.hotel h
       JOIN h.owner ho
       JOIN b.user u
       LEFT JOIN b.rooms r
       LEFT JOIN b.invoice i
       WHERE ho.id = :ownerId
       """)
    List<Object[]> findBookingsByHotelOwnerId(Long ownerId);
    Booking findByBookingCode(String bookingCode);
    @Query("SELECT SUM(b.invoice.totalPrice) FROM Booking b " +
            "WHERE YEAR(b.checkInDate) = :year AND MONTH(b.checkInDate) = :month")
    BigDecimal calculateMonthlyRevenue(@Param("year") int year, @Param("month") int month);

    @Query("SELECT SUM(b.invoice.totalPrice) FROM Booking b " +
            "WHERE YEAR(b.checkInDate) = :year AND MONTH(b.checkInDate) = :month " +
            "AND b.hotel.id = :hotelId")
    BigDecimal calculateMonthlyRevenueForHotel(@Param("year") int year, @Param("month") int month, @Param("hotelId") Long hotelId);



    @Query("SELECT b FROM Booking b WHERE b.bookingCode = :bookingCode AND b.user.id = :userId")
    Optional<Booking> findByBookingCodeAndUserId(@Param("bookingCode") String bookingCode, @Param("userId") Long userId);

    @Query("SELECT b FROM Booking b WHERE b.bookingCode = :bookingCode")
    Optional<Booking> findByBookingCode1(String bookingCode);
}
