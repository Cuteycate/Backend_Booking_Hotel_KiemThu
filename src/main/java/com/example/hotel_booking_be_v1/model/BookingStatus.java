package com.example.hotel_booking_be_v1.model;

public enum BookingStatus {
    PENDING,    // Đang chờ xử lý
    CONFIRMED,  // Đã xác nhận
    CANCELLED,  // Đã hủy
    COMPLETED,   // Đã hoàn thành
    DEPOSITED    // Đã đặt cọc
}