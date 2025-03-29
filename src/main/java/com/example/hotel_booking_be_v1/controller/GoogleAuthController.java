package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.model.Role;
import com.example.hotel_booking_be_v1.model.User;
import com.example.hotel_booking_be_v1.repository.RoleRepository;
import com.example.hotel_booking_be_v1.repository.UserRepository;
import com.example.hotel_booking_be_v1.security.JwtUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> request) {
        String googleIdToken = request.get("id_token");  // Lấy id_token từ frontend

        // Xác thực token với Google
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList("996395502261-ahd9mb7p16kk36en02m0sl842k0o7ucr.apps.googleusercontent.com"))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(googleIdToken);
            if (idToken == null) {
                return ResponseEntity.badRequest().body("Invalid Google ID token.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // Kiểm tra user đã tồn tại chưa
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                // Tạo mới user nếu chưa tồn tại
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setFirstName(name);
                Role role = roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() -> new RuntimeException("Role not found"));
                newUser.setRoles(Set.of(role));
                newUser.setEmailVerified(true);
                newUser.setPassword(passwordEncoder.encode("google_oauth"));  // Mật khẩu giả
                return userRepository.save(newUser);
            });

            // Tạo JWT cho hệ thống của bạn
            String jwt = jwtUtils.generateJwtTokenForGoogle(Map.of("email", email, "name", name));
            return ResponseEntity.ok(Map.of("token", jwt));
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.badRequest().body("Error verifying Google ID token.");
        }
    }
}

