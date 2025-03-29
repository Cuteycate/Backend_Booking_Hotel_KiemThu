package com.example.hotel_booking_be_v1.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String massage) {
        super(massage);

    }
}
