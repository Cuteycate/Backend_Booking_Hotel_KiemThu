package com.example.hotel_booking_be_v1.service;


import com.example.hotel_booking_be_v1.model.Booking;
import com.example.hotel_booking_be_v1.model.Invoice;
import com.example.hotel_booking_be_v1.model.InvoiceDetail;
import com.example.hotel_booking_be_v1.model.Room;
import com.example.hotel_booking_be_v1.repository.BookingRepository;
import com.example.hotel_booking_be_v1.repository.InvoiceRepository;
import com.example.hotel_booking_be_v1.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;

    public Invoice createInvoiceForBooking(Booking booking) {
        // Lưu booking vào database trước
        bookingRepository.save(booking);

        // Sau khi booking đã lưu, tạo đối tượng Invoice
        Invoice invoice = new Invoice();
        invoice.setBooking(booking); // Gán booking đã lưu vào hóa đơn

        BigDecimal totalPrice = BigDecimal.ZERO;
        long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());

        // Nếu số ngày hợp lệ
        if (days > 0) {
            // Khởi tạo danh sách details nếu chưa có
            if (invoice.getDetails() == null) {
                invoice.setDetails(new ArrayList<>());
            }

            // Tính tổng giá cho các phòng trong booking dựa trên số ngày
            for (Room room : booking.getRooms()) {
                InvoiceDetail detail = new InvoiceDetail();
                detail.setRoom(room);
                detail.setPrice(room.getRoomPrice());
                detail.setNumberOfGuests(booking.getNumberOfGuests());

                BigDecimal roomTotalPrice = room.getRoomPrice().multiply(BigDecimal.valueOf(days));
                totalPrice = totalPrice.add(roomTotalPrice);

                detail.setInvoice(invoice);
                invoice.getDetails().add(detail);
            }


            invoice.setTotalPrice(totalPrice); // Cập nhật tổng giá trị hóa đơn
        } else {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }

        // Lưu hóa đơn vào database
        invoiceRepository.save(invoice);

        return invoice;
    }
}
