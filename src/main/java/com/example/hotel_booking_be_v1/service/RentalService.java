package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.exception.UserAlreadyExistsException;
import com.example.hotel_booking_be_v1.model.Role;
import com.example.hotel_booking_be_v1.model.User;
import com.example.hotel_booking_be_v1.repository.RoleRepository;
import com.example.hotel_booking_be_v1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class RentalService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerRental(User rentalUser) {
        // Kiểm tra xem email đã tồn tại chưa
        if(userRepository.existsByEmail(rentalUser.getEmail())){
            throw new UserAlreadyExistsException(rentalUser.getEmail() + " already exists.");
        }

        // Mã hóa mật khẩu
        rentalUser.setPassword(passwordEncoder.encode(rentalUser.getPassword()));
        System.out.println(rentalUser.getPassword());

        // Gán vai trò "ROLE_USER"
        Role userRole = roleRepository.findByName("ROLE_RENTAL").get();
        rentalUser.setRoles(Collections.singletonList(userRole));

        // Thiết lập tài khoản rental chưa được phê duyệt
        rentalUser.setApproved(false);  // Ban đầu tài khoản rental chưa được phê duyệt

        // Lưu tài khoản rental vào cơ sở dữ liệu
        return userRepository.save(rentalUser);
    }

    public List<User> getAllRentals() {
        return userRepository.findByRolesName("ROLE_RENTAL");
    }

    public void approveRental(Long rentalId) {
        User rentalUser = userRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental user not found"));
        rentalUser.setApproved(true); // Cập nhật trạng thái được phê duyệt
        userRepository.save(rentalUser);
    }
}
