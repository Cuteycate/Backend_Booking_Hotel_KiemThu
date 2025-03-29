package com.example.hotel_booking_be_v1.response;

import com.example.hotel_booking_be_v1.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BookingStatus status; // Thêm trường status
    private String email;
    private String name;
    private String hotelName;
    private List<String> roomNames = new ArrayList<>();
    private BigDecimal totalPrice;
    private BigDecimal depositAmount;
    private Date paymentDate;
    private String paymentMethod;
    private boolean hasReview;
}

