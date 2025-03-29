package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.exception.UserAlreadyExistsException;
import com.example.hotel_booking_be_v1.model.ChangePasswordRequest;
import com.example.hotel_booking_be_v1.model.EmailVerificationToken;
import com.example.hotel_booking_be_v1.model.User;
import com.example.hotel_booking_be_v1.repository.EmailVerificationTokenRepository;
import com.example.hotel_booking_be_v1.request.LoginRequest;
import com.example.hotel_booking_be_v1.response.JwtResponse;
import com.example.hotel_booking_be_v1.security.HotelUserDetails;
import com.example.hotel_booking_be_v1.security.JwtUtils;
import com.example.hotel_booking_be_v1.service.IUserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IUserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmailVerificationTokenRepository tokenRepository;

    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@RequestBody User user){
        try{
            userService.registerUser(user);
            return ResponseEntity.ok("Registration successful. Please check your email to verify your account.");

        }catch (UserAlreadyExistsException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> authenticatedUser(@Valid @RequestBody LoginRequest loginRequest){
        try {
            // Attempt to authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token if authentication is successful
            String jwt = jwtUtils.generateJwtTokenForUser(authentication);
            HotelUserDetails userDetails = (HotelUserDetails) authentication.getPrincipal();

            // Check if the user's email is verified
            if (!userDetails.isEmailVerified()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email chưa được xác minh!");
            }

            // Retrieve roles assigned to the user
            List<String> roles = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            // Return the JWT response with user information and roles
            return ResponseEntity.ok(new JwtResponse(
                    userDetails.getId(),
                    userDetails.getEmail(), jwt, roles
            ));
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            // Handle case where username or password is incorrect
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password. Please try again.");
        } catch (Exception e) {
            // Handle other unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred. Please try again.");
        }
    }
    @PostMapping("/admin-login")
    public ResponseEntity<?> authenticatedAdmin(@Valid @RequestBody LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        HotelUserDetails userDetails = (HotelUserDetails) authentication.getPrincipal();

        // Kiểm tra xem người dùng có phải là admin không
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Chỉ admin mới có quyền truy cập.");
        }

        // Tạo token nếu là admin
        String jwt = jwtUtils.generateJwtTokenForUser(authentication);
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(new JwtResponse(
                userDetails.getId(),
                userDetails.getEmail(), jwt, roles
        ));
    }
    @PostMapping("/login-rental")
    public ResponseEntity<?> authenticatedRenter(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        ); //1
        SecurityContextHolder.getContext().setAuthentication(authentication); //2
        HotelUserDetails userDetails = (HotelUserDetails) authentication.getPrincipal(); //3

        // Kiểm tra nếu người dùng có vai trò "RENTAL"
        boolean isRenter = userDetails.getAuthorities().stream() //4
                .anyMatch(role -> role.getAuthority().equals("ROLE_RENTAL"));
         if (!isRenter) { //5
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Chỉ rental mới có quyền truy cập.");    //7
        }
        // Kiểm tra nếu tài khoản rental đã được xác nhận
        if (!userDetails.isApproved()) { //6
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tài khoản rental chưa được xác nhận.");        //9
        }
        // Generate token if the user is a renter and has been approved
        String jwt = jwtUtils.generateJwtTokenForUser(authentication);      //8
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();          //10

        return ResponseEntity.ok(new JwtResponse(       //11
                userDetails.getId(),
                userDetails.getEmail(), jwt, roles
        ));
    }

    @GetMapping("/verify-email")
    public void verifyEmail(@RequestParam("token") String token, HttpServletResponse response) {
        try {
            userService.verifyEmail(token); // Xác minh email với token
            response.setStatus(HttpServletResponse.SC_FOUND); // HTTP 302: Chuyển hướng
            response.setHeader("Location", "http://localhost:5173/login?message=Email verified successfully"); // Địa chỉ trang FE
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // HTTP 400
        }
    }
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute ChangePasswordRequest request) {

        try {
            // Lấy email từ UserDetails
            String email = userDetails.getUsername();

            User user = userService.    getUserByEmail(email);
            // Gọi service để đổi mật khẩu
            userService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());

            return ResponseEntity.ok("Mật khẩu đã được thay đổi thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        try {
            userService.sendPasswordResetEmail(email);
            return ResponseEntity.ok("Password reset email sent successfully.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    // Endpoint thay đổi mật khẩu
    @GetMapping("/reset-password")
    public RedirectView verifyTokenAndRedirect(@RequestParam("token") String token) {
        // Kiểm tra token có hợp lệ không
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token"));

        // Kiểm tra token có hết hạn không
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token has expired");
        }

        // Nếu token hợp lệ, thực hiện chuyển hướng tới URL frontend
        String frontendResetPasswordUrl = "http://localhost:5173/reset-password?token=" + token;

        // Trả về RedirectView để chuyển hướng người dùng
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(frontendResetPasswordUrl);
        return redirectView;
    }
    @PostMapping("/password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Password reset successfully.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

}
