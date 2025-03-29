package com.example.hotel_booking_be_v1;

import com.example.hotel_booking_be_v1.model.Booking;
import com.example.hotel_booking_be_v1.model.Hotel;
import com.example.hotel_booking_be_v1.model.Room;
import com.example.hotel_booking_be_v1.model.User;
import com.example.hotel_booking_be_v1.service.BookingService;
import com.example.hotel_booking_be_v1.service.HotelService;
import com.example.hotel_booking_be_v1.service.RoomService;
import com.example.hotel_booking_be_v1.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private HotelService hotelService;

    @MockBean
    private RoomService roomService;

    @MockBean
    private BookingService bookingService;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = getAuthToken();
    }

    private String getAuthToken() throws Exception {
        String loginJson = "{ \"email\": \"test123@gmail.com\", \"password\": \"123\" }";

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk()) // Kiểm tra xem login thành công
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return new JSONObject(response).getString("token"); // Giả sử API trả về JSON có key "token"
    }


    @Test
    void testUserNotFound() throws Exception {
        when(userService.getUserByEmail("User@gmail.com")).thenReturn(null);

        mockMvc.perform(post("/bookings/add")
                        .header("Authorization", "Bearer " + token)
                        .param("email", "User@gmail.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found."));
    }

    @Test
    void testHotelNotFound() throws Exception {
        User user = new User();
        when(userService.getUserByEmail("test123@gmail.com")).thenReturn(user);
        when(hotelService.getHotelById1(39L)).thenReturn(null);

        mockMvc.perform(post("/bookings/add")
                        .header("Authorization", "Bearer " + token)
                        .param("hotelId", "39"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Hotel not found."));
    }

    @Test
    void testRoomNotFound() throws Exception {
        User user = new User();
        Hotel hotel = new Hotel();
        when(userService.getUserByEmail("test123@gmail.com")).thenReturn(user);
        when(hotelService.getHotelById1(6L)).thenReturn(hotel);
        when(roomService.getAllRoomByIds(anyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/bookings/add")
                        .header("Authorization", "Bearer " + token)
                        .param("hotelId", "6")
                        .param("roomIds", "7"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Room with ID 7 not found."));
    }

    @Test
    void testNotEnoughRoomsAvailable() throws Exception {
        User user = new User();
        Hotel hotel = new Hotel();
        Room room = new Room();
        room.setId(10L);
        room.setQuantity(1); // Set quantity to 1 to ensure "not enough rooms" scenario

        // Create a booking with rooms for the overlapping booking
        Booking existingBooking = new Booking();
        existingBooking.setRooms(Collections.singletonList(room));

        // Setup mocks
        when(userService.getUserByEmail("test123@gmail.com")).thenReturn(user);
        when(hotelService.getHotelById1(6L)).thenReturn(hotel);
        when(roomService.getAllRoomByIds(anyList())).thenReturn(List.of(room));
        when(bookingService.findOverlappingBookings(anyLong(), any(), any()))
            .thenReturn(List.of(existingBooking));

        // Perform test
        mockMvc.perform(post("/bookings/add")
                .header("Authorization", "Bearer " + token)
                .param("hotelId", "6")
                .param("roomIds", "10")
                .param("checkInDate", "2025-04-10")
                .param("checkOutDate", "2025-04-15"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Not enough rooms available")));
    }

    @Test
    void testInvalidCheckoutDate() throws Exception {
        User user = new User();
        Hotel hotel = new Hotel();
        Room room = new Room();

        room.setId(10L);
        room.setQuantity(6);

        Booking existingBooking = new Booking();
        existingBooking.setRooms(Collections.singletonList(room));

        when(userService.getUserByEmail("test123@gmail.com")).thenReturn(user);
        when(hotelService.getHotelById1(6L)).thenReturn(hotel);
        when(roomService.getAllRoomByIds(anyList())).thenReturn(List.of(room));
        when(bookingService.findOverlappingBookings(anyLong(), any(), any()))
                .thenReturn(List.of(existingBooking));

        mockMvc.perform(post("/bookings/add")
                .header("Authorization", "Bearer " + token)
                .param("hotelId", "6")
                .param("roomIds", "10")
                .param("checkInDate", "2025-04-10")
                .param("checkOutDate", "2025-04-09")
                .param("numberOfGuests", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Check-out date must be after check-in date."));
    }

    @Test
    void testBookingSuccess() throws Exception {
        // Setup test data
        User user = new User();
        Hotel hotel = new Hotel();
        Room room = new Room();
        room.setId(10L);
        room.setQuantity(2);

        // Setup mocks
        when(userService.getUserByEmail("test123@gmail.com")).thenReturn(user); // Fixed email to match token
        when(hotelService.getHotelById1(6L)).thenReturn(hotel);
        when(roomService.getAllRoomByIds(anyList())).thenReturn(List.of(room));
        when(bookingService.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        doNothing().when(bookingService).saveBookingWithInvoice(any(Booking.class));

        // Perform test
        mockMvc.perform(post("/bookings/add")
                .header("Authorization", "Bearer " + token)
                .param("hotelId", "6")
                .param("roomIds", "10")
                .param("checkInDate", "2025-04-05")
                .param("checkOutDate", "2025-04-09")
                .param("numberOfGuests", "2"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Booking created successfully with invoice."));

        // Verify service calls
        verify(bookingService).saveBookingWithInvoice(any(Booking.class));
    }
}
