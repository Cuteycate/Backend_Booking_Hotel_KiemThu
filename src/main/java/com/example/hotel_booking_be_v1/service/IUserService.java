package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.model.User;
import com.example.hotel_booking_be_v1.response.UserResponse;

import java.util.List;

public interface IUserService {
    User registerUser(User user);
    List<User> getUsers();
    void deleteUser(String email);
    User getUser(String email);
    User getUserById (Long id);
    User getUserByEmail(String email);
    boolean verifyEmail(String token);
    void changePassword(Long userId, String oldPassword, String newPassword);
    UserResponse getUserResponse(Long userId);
    public void sendPasswordResetEmail(String email);
    public void resetPassword(String token, String newPassword);
}

