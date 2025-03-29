package com.example.hotel_booking_be_v1.exception;

public class InternalServerException extends RuntimeException {
    public InternalServerException(String massage) {
        super(massage);
    }
}
