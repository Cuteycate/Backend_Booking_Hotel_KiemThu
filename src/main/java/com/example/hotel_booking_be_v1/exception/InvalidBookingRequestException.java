package com.example.hotel_booking_be_v1.exception;

public class InvalidBookingRequestException extends RuntimeException {
    public InvalidBookingRequestException(String message){
        super(message);
    }
}
