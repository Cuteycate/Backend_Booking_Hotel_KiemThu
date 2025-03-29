package com.example.hotel_booking_be_v1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class InvoiceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore // Tránh vòng lặp với Invoice
    private Invoice invoice; // Liên kết với Invoice

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnore
    private Room room; // Liên kết với Room

    @Column(nullable = false)
    private BigDecimal price; // Giá phòng

    @Column(nullable = false)
    private int numberOfGuests; // Số lượng khách (số người trong booking)

}