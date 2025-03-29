package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.Hotel;
import com.example.hotel_booking_be_v1.model.MonthlyRevenue;
import com.example.hotel_booking_be_v1.model.RevenueForHotel;
import com.example.hotel_booking_be_v1.repository.BookingRepository;
import com.example.hotel_booking_be_v1.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service

public class RevenueService {

    @Autowired
    private final BookingRepository bookingRepository;
    @Autowired
    private final HotelRepository hotelRepository;

    public RevenueService(BookingRepository bookingRepository, HotelRepository hotelRepository) {
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
    }

    public List<MonthlyRevenue> calculateRevenueForYear(int year) {
        List<MonthlyRevenue> revenues = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal totalRevenue = bookingRepository.calculateMonthlyRevenue(year, month);
            revenues.add(new MonthlyRevenue(month, year, totalRevenue != null ? totalRevenue : BigDecimal.ZERO));
        }
        return revenues;
    }

    public List<RevenueForHotel> calculateRevenueForUser(int year, Long Id) {
        List<RevenueForHotel> revenues = new ArrayList<>();

        // Lấy danh sách khách sạn mà người dùng sở hữu
        List<Hotel> hotels = hotelRepository.findAllByOwnerId(Id); // Giả sử bạn đã có phương thức này

        for (Hotel hotel : hotels) {
            List<MonthlyRevenue> monthlyRevenues = new ArrayList<>();

            // Tính doanh thu cho từng tháng trong năm
            for (int month = 1; month <= 12; month++) {
                BigDecimal totalRevenue = bookingRepository.calculateMonthlyRevenueForHotel(year, month, hotel.getId());
                monthlyRevenues.add(new MonthlyRevenue(month, year, totalRevenue != null ? totalRevenue : BigDecimal.ZERO));
            }

            revenues.add(new RevenueForHotel(hotel.getId(), hotel.getName(), monthlyRevenues));
        }

        return revenues;
    }
}
