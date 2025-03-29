package com.example.hotel_booking_be_v1.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Ward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int code;

    private String codeName;

    private String divisionType;

    private String shortCodeName;

    @ManyToOne
    @JoinColumn(name = "district_id")
    private District district;
}