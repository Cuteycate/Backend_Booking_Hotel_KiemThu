package com.example.hotel_booking_be_v1.exception;

public class PhotoRetrievalException extends RuntimeException {
    public PhotoRetrievalException(String massage) {
        super(massage);
    }
}
