package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.model.BookingDTO;
import com.example.hotel_booking_be_v1.model.Booking;
import com.example.hotel_booking_be_v1.model.Hotel;
import com.example.hotel_booking_be_v1.model.Room;
import com.example.hotel_booking_be_v1.model.User;
import com.example.hotel_booking_be_v1.service.BookingService;
import com.example.hotel_booking_be_v1.service.HotelService;
import com.example.hotel_booking_be_v1.service.RoomService;
import com.example.hotel_booking_be_v1.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookingControllerTest {

    @InjectMocks
    private BookingController bookingController;

    @Mock
    private UserService userService;

    @Mock
    private HotelService hotelService;

    @Mock
    private RoomService roomService;

    @Mock
    private BookingService bookingService;

    @Mock
    private UserDetails userDetails;

    private User testUser;
    private Hotel testHotel;
    private Room testRoom;
    private BookingDTO testBookingDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        // Setup test hotel
        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");

        // Setup test room
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setQuantity(5);
        testRoom.setHotel(testHotel);

        // Setup test booking DTO
        testBookingDTO = new BookingDTO();
        testBookingDTO.setHotelId(1L);
        testBookingDTO.setCheckInDate(LocalDate.now().plusDays(1));
        testBookingDTO.setCheckOutDate(LocalDate.now().plusDays(3));
        testBookingDTO.setNumberOfGuests(2);
        testBookingDTO.setStatus("PENDING");
        testBookingDTO.setRoomIds(Arrays.asList(1L, 1L)); // Requesting 2 rooms of type 1
    }

    @Test
    void addBooking_Success() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
        when(hotelService.getHotelById1(anyLong())).thenReturn(testHotel);
        when(roomService.getAllRoomByIds(any())).thenReturn(Arrays.asList(testRoom));
        when(bookingService.findOverlappingBookings(anyLong(), any(), any())).thenReturn(new ArrayList<>());
        doNothing().when(bookingService).saveBookingWithInvoice(any(Booking.class));

        // Act
        ResponseEntity<?> response = bookingController.addBooking(userDetails, testBookingDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Booking created successfully with invoice.", response.getBody());
        verify(bookingService).saveBookingWithInvoice(any(Booking.class));
    }

    @Test
    void addBooking_UserNotFound() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("nonexistent@example.com");
        when(userService.getUserByEmail(anyString())).thenReturn(null);

        // Act
        ResponseEntity<?> response = bookingController.addBooking(userDetails, testBookingDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", response.getBody());
    }

    @Test
    void addBooking_HotelNotFound() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
        when(hotelService.getHotelById1(anyLong())).thenReturn(null);

        // Act
        ResponseEntity<?> response = bookingController.addBooking(userDetails, testBookingDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Hotel not found.", response.getBody());
    }

    @Test
    void addBooking_NotEnoughRoomsAvailable() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
        when(hotelService.getHotelById1(anyLong())).thenReturn(testHotel);
        when(roomService.getAllRoomByIds(any())).thenReturn(Arrays.asList(testRoom));
        
        // Simulate that all rooms are booked
        List<Booking> overlappingBookings = new ArrayList<>();
        Booking existingBooking = new Booking();
        existingBooking.setRooms(Arrays.asList(testRoom, testRoom, testRoom, testRoom, testRoom));
        overlappingBookings.add(existingBooking);
        when(bookingService.findOverlappingBookings(anyLong(), any(), any())).thenReturn(overlappingBookings);

        // Act
        ResponseEntity<?> response = bookingController.addBooking(userDetails, testBookingDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Not enough rooms available"));
    }

    @Test
    void addBooking_InvalidCheckOutDate() {
        // Arrange
        testBookingDTO.setCheckOutDate(LocalDate.now());
        testBookingDTO.setCheckInDate(LocalDate.now().plusDays(1));

        // Act
        ResponseEntity<?> response = bookingController.addBooking(userDetails, testBookingDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Check-out date must be after check-in date.", response.getBody());
    }
} 