package com.example.hotel_booking_be_v1.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@Entity

public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal totalPrice; // Tổng giá trị của hóa đơn

    @OneToOne(optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnore // Tránh vòng lặp giữa Invoice và Booking
    private Booking booking; // Liên kết với Booking

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Tránh vòng lặp với InvoiceDetail
    private List<InvoiceDetail> details = new ArrayList<>(); // Chi tiết hóa đơn

    @Column
    private BigDecimal depositAmount; // Tiền trả trước

    @Column
    private Date paymentDate; // Ngày thanh toán

    @Column
    private String paymentMethod; // Phương thức thanh toán (VD: VNPay, Cash, etc.)
}

