package com.example.hotel_booking_be_v1.repository;

import com.example.hotel_booking_be_v1.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // Các phương thức truy vấn hóa đơn có thể thêm vào đây
}
