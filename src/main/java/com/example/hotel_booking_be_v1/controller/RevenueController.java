package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.model.Invoice;
import com.example.hotel_booking_be_v1.model.MonthlyRevenue;
import com.example.hotel_booking_be_v1.model.RevenueForHotel;
import com.example.hotel_booking_be_v1.model.User;
import com.example.hotel_booking_be_v1.repository.InvoiceRepository;
import com.example.hotel_booking_be_v1.service.IUserService;
import com.example.hotel_booking_be_v1.service.RevenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/revenue")
public class RevenueController {

    private final RevenueService revenueService;
    private final IUserService userService;

    public RevenueController(RevenueService revenueService, IUserService userService) {
        this.revenueService = revenueService;
        this.userService = userService;
    }

    // API lấy dữ liệu doanh thu trong 12 tháng cho tất cả các khách sạn
    @GetMapping("/all/{year}")
    public ResponseEntity<List<MonthlyRevenue>> getRevenueForAllHotels(
            @PathVariable int year) {
        List<MonthlyRevenue> revenues = revenueService.calculateRevenueForYear(year);
        return ResponseEntity.ok(revenues);
    }

    // API lấy dữ liệu doanh thu trong 12 tháng cho các khách sạn của một user cụ thể
    @GetMapping("/user/{year}")
    public ResponseEntity<List<RevenueForHotel>> getRevenueForUserHotels(
            @PathVariable int year,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Lấy email từ userDetails
        String email = userDetails.getUsername();

        // Tìm user bằng email
        User user = userService.getUserByEmail(email);
        List<RevenueForHotel> revenues = revenueService.calculateRevenueForUser(year, user.getId());
        return ResponseEntity.ok(revenues);
    }
}
