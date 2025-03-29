package com.example.hotel_booking_be_v1.model;

import com.example.hotel_booking_be_v1.service.IRoomService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingDTO {

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private int numberOfGuests;

    private BookingStatus status;

    private Long hotelId;

    private List<Long> roomIds;
}
