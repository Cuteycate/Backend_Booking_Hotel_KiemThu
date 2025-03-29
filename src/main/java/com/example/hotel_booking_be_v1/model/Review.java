package com.example.hotel_booking_be_v1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int rating; // Số sao (1-5)

    @Column(columnDefinition = "TEXT")
    private String content; // Nội dung đánh giá

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonIgnore
    private List<ReviewPhoto> photos = new ArrayList<>(); // Danh sách ảnh khác

    @OneToOne(optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking; // Liên kết với booking thay vì user và hotel

    @Column(nullable = false)
    private LocalDate reviewDate = LocalDate.now(); // Ngày đánh giá
}

