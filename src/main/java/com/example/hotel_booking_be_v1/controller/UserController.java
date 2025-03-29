package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.model.User;
import com.example.hotel_booking_be_v1.repository.UserRepository;
import com.example.hotel_booking_be_v1.response.UserResponse;
import com.example.hotel_booking_be_v1.service.IUserService;
import com.example.hotel_booking_be_v1.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final UserRepository userRepository;


    @GetMapping("/all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        // Fetch all users from the database
        List<User> users = userRepository.findAll();

        // Transform the list of users into the UserResponse format
        List<UserResponse> userResponses = users.stream()
                .map(user -> {
                    String roleName = null;

                    // Check if the user has roles
                    if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                        // Assuming the user has only one role, get the first role's name
                        roleName = user.getRoles().iterator().next().getName();
                    }

                    // Return the transformed UserResponse object
                    return new UserResponse(
                            user.getId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getEmail(),
                            roleName // Set the role name, or null if no role
                    );
                })
                .collect(Collectors.toList());

        // Return the list of transformed user responses
        return ResponseEntity.ok(userResponses);
    }
    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and #email == principal.username)")
    public ResponseEntity<String> deleteUser(@PathVariable("userId") String email){
        try{
            userService.deleteUser(email);
            return ResponseEntity.ok("User deleted successfully");

        }catch (UsernameNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user: " + e.getMessage());
        }
    }
    @GetMapping("/user")
    public UserResponse getUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userService.getUserByEmail(email);
        return userService.getUserResponse(user.getId());
    }
}
