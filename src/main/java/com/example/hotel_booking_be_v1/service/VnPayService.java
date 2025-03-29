package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.config.Config;
import com.example.hotel_booking_be_v1.model.Booking;
import com.example.hotel_booking_be_v1.model.BookingDTO;
import com.example.hotel_booking_be_v1.model.Invoice;
import com.example.hotel_booking_be_v1.model.User;
import com.example.hotel_booking_be_v1.repository.BookingRepository;
import com.example.hotel_booking_be_v1.repository.InvoiceRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.example.hotel_booking_be_v1.model.BookingStatus.DEPOSITED;

@Service
@RequiredArgsConstructor
public class VnPayService {

    private final BookingRepository bookingRepository;

    private final InvoiceRepository invoiceRepository;


    public String createPaymentUrl(HttpServletRequest request, Long amount, BookingDTO bookingDTO, String email) throws Exception {
        long amounts = amount;
        List<Long> roomIds = bookingDTO.getRoomIds();
        String roomIdsString = roomIds.toString(); // Sử dụng phương thức toString() của List để chuyển thành chuỗi theo định dạng [12, 12]
        String vnpVersion = Config.vnp_Version;
        String vnpCommand = Config.vnp_Command;
        String orderInfo = "Info:" + bookingDTO.getHotelId() + "?" + roomIdsString + "?" + bookingDTO.getCheckInDate() + "?" + bookingDTO.getCheckOutDate() + "?" + bookingDTO.getNumberOfGuests() + "?" + email ;
        String orderType = "other";
        String txnRef = Config.getRandomNumber(8);

        String ipAddress = Config.getIpAddress(request);
        String tmnCode = Config.vnp_TmnCode;

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnpVersion);
        vnpParams.put("vnp_Command", vnpCommand);
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amounts * 100)); // VNPay yêu cầu số tiền tính theo VND * 100
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_BankCode", "NCB");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_ReturnUrl",  "http://localhost:8080/api/bookings/payment/vnpay-return");
        vnpParams.put("vnp_IpAddr", "13.160.92.202");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = Config.hmacSHA512(Config.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;
        return paymentUrl;
    }
}
