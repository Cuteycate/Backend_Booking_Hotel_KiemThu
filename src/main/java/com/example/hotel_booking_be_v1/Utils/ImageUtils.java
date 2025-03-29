package com.example.hotel_booking_be_v1.Utils;

import java.sql.Blob;
import java.util.Base64;

public class ImageUtils {
    public static String encodePhoto(Blob blob) throws Exception {
        byte[] bytes = blob.getBytes(1, (int) blob.length());
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static Blob decodePhoto(String base64) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(base64);
        return new javax.sql.rowset.serial.SerialBlob(bytes);
    }
}
