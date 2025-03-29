package com.example.hotel_booking_be_v1.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MonthlyRevenue {
    private int month;
    private int year;
    private BigDecimal totalRevenue;

    public MonthlyRevenue(int month, int year, BigDecimal totalRevenue) {
        this.month = month;
        this.year = year;
        this.totalRevenue = totalRevenue;
    }

}

