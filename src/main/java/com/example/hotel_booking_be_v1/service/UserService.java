package com.example.hotel_booking_be_v1.service;

import com.example.hotel_booking_be_v1.exception.UserAlreadyExistsException;
import com.example.hotel_booking_be_v1.model.EmailVerificationToken;
import com.example.hotel_booking_be_v1.model.Role;
import com.example.hotel_booking_be_v1.model.User;
import com.example.hotel_booking_be_v1.repository.EmailVerificationTokenRepository;
import com.example.hotel_booking_be_v1.repository.RoleRepository;
import com.example.hotel_booking_be_v1.repository.UserRepository;
import com.example.hotel_booking_be_v1.response.UserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    @Override
    public User registerUser(User user) {
        if(userRepository.existsByEmail(user.getEmail())){
            throw new UserAlreadyExistsException(user.getEmail()+"already exist");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println(user.getPassword());
        Role userRole = roleRepository.findByName("ROLE_USER").get();
        user.setRoles(Collections.singletonList(userRole));
        User savedUser = userRepository.save(user);
        // Tạo token xác minh email
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(savedUser);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // Token hết hạn sau 24 giờ
        tokenRepository.save(verificationToken);

        // Gửi email xác minh
        String verificationLink = "http://localhost:8080/auth/verify-email?token=" + token;
        String subject = "Email Verification";
        String body = "Hi " + savedUser.getFirstName() + ",\n\n" +
                "Please click the link below to verify your email:\n\n" + verificationLink + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "Best regards,\n" +
                "Booking Hotel";

        emailService.sendMail(savedUser.getEmail(), subject, body);

        return savedUser;
    }

    // Gửi email quên mật khẩu
    public void sendPasswordResetEmail(String email) {
        // Tìm người dùng theo email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));

        // Tạo token quên mật khẩu
        String token = UUID.randomUUID().toString();

        // Tạo đối tượng EmailVerificationToken
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // Token hết hạn sau 24 giờ

        // Lưu token vào cơ sở dữ liệu
        tokenRepository.save(verificationToken);

        // Tạo đường dẫn reset mật khẩu
        String resetLink = "http://localhost:8080/auth/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String body = "Hi " + user.getFirstName() + ",\n\n" +
                "Please click the link below to reset your password:\n\n" + resetLink + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "Best regards,\n" +
                "Your Team";

        // Gửi email cho người dùng
        emailService.sendMail(user.getEmail(), subject, body);
    }

    // Đổi mật khẩu khi nhận được token hợp lệ
    public void resetPassword(String token, String newPassword) {
        // Kiểm tra token có hợp lệ không
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token"));

        // Kiểm tra xem token có hết hạn chưa
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token has expired");
        }

        // Lấy người dùng từ token
        User user = verificationToken.getUser();

        // Mã hóa mật khẩu mới và cập nhật vào user
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Xóa token sau khi sử dụng (tuỳ chọn)
        tokenRepository.delete(verificationToken);
    }


    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }
    @Transactional
    @Override
    public void deleteUser(String email) {
        User theUser = getUser(email);
        if (theUser != null){
            userRepository.deleteByEmail(email);
        }
    }

    @Override
    public User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("user not found "));
    }
    @Override
    public UserResponse getUserResponse(Long userId) {
        // Lấy thông tin người dùng từ DB
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Tạo đối tượng UserResponse và gán giá trị từ User
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setEmail(user.getEmail());

        // Giả sử lấy tên role đầu tiên của user (nếu có nhiều role)
        String roleName = user.getRoles().isEmpty() ? "" : user.getRoles().iterator().next().getName();
        userResponse.setRoleName(roleName);

        // Trả về UserResponse đã đầy đủ dữ liệu
        return userResponse;
    }
    @Override
    public User getUserById (Long id){
        return userRepository.findById(id).orElse(null);
    }
    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null); // Hoặc ném exception nếu bạn muốn
    }
    @Override
    public boolean verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc đã hết hạn."));

        User user = verificationToken.getUser();
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token đã hết hạn.");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken); // Xóa token sau khi sử dụng
        return true;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // 1. Tìm User theo ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));

        // 2. Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác.");
        }

        // 3. Mã hóa và cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


}
