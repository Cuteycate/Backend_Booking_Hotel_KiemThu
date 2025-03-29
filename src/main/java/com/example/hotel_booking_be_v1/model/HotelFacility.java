package com.example.hotel_booking_be_v1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Getter
@Setter
public class HotelFacility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "facilities", cascade = {CascadeType.MERGE, CascadeType.PERSIST}) // Tên thuộc tính trong Hotel
    @JsonIgnore
    private List<Hotel> hotels = new ArrayList<>();




}