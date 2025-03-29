package com.example.hotel_booking_be_v1;

import com.example.hotel_booking_be_v1.controller.AuthController;
import com.example.hotel_booking_be_v1.request.LoginRequest;
import com.example.hotel_booking_be_v1.response.JwtResponse;
import com.example.hotel_booking_be_v1.security.JwtUtils;
import com.example.hotel_booking_be_v1.security.HotelUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Class test cho AuthenticationController
 * Sử dụng Mockito để giả lập các dependency
 */
@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

    // Giả lập AuthenticationManager để xử lý việc xác thực
    @Mock
    private AuthenticationManager authenticationManager;

    // Giả lập JwtUtils để xử lý việc tạo JWT token
    @Mock
    private JwtUtils jwtUtils;

    // Controller chính được test, các dependency sẽ được tự động inject
    @InjectMocks
    private AuthController authController;

    // Các biến được sử dụng trong các test case
    private LoginRequest loginRequest;        // Đối tượng chứa thông tin đăng nhập
    private HotelUserDetails userDetails;     // Thông tin chi tiết của người dùng
    private Authentication authentication;    // Đối tượng xác thực
    private List<GrantedAuthority> authorities; // Danh sách quyền của người dùng

    /**
     * Phương thức được chạy trước mỗi test case
     * Khởi tạo các đối tượng cần thiết cho việc test
     */
    @BeforeEach
    void setUp() {
        // Khởi tạo thông tin đăng nhập
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
        
        // Khởi tạo quyền RENTAL cho người dùng
        authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_RENTAL"));
        
        // Tạo đối tượng giả lập một phần cho HotelUserDetails
        userDetails = spy(new HotelUserDetails());
        
        // Tạo đối tượng giả lập cho Authentication
        authentication = mock(Authentication.class);
    }

    /**
     * Test case: Đăng nhập thành công với tài khoản RENTAL đã được xác nhận
     */
    @Test
    void loginRental_Success() {
        // Arrange: Chuẩn bị dữ liệu và mock các hành vi
        doReturn(authorities).when(userDetails).getAuthorities();  // Mock quyền RENTAL
        doReturn(1L).when(userDetails).getId();                    // Mock ID người dùng
        doReturn("test@example.com").when(userDetails).getEmail(); // Mock email
        doReturn(true).when(userDetails).isApproved();            // Mock trạng thái đã xác nhận
        when(authentication.getPrincipal()).thenReturn(userDetails); // Mock thông tin người dùng trong authentication
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);                       // Mock quá trình xác thực thành công
        when(jwtUtils.generateJwtTokenForUser(authentication)).thenReturn("jwt-token"); // Mock tạo JWT token

        // Act: Thực hiện phương thức cần test
        ResponseEntity<?> response = authController.authenticatedRenter(loginRequest);

        // Assert: Kiểm tra kết quả
        assertNotNull(response);                                  // Kiểm tra response không null
        assertEquals(HttpStatus.OK, response.getStatusCode());     // Kiểm tra status code 200
        assertTrue(response.getBody() instanceof JwtResponse);     // Kiểm tra kiểu dữ liệu response
        // Kiểm tra chi tiết JWT response
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertEquals(1L, jwtResponse.getId());                     // Kiểm tra ID
        assertEquals("test@example.com", jwtResponse.getEmail());  // Kiểm tra email
        assertEquals("jwt-token", jwtResponse.getToken());         // Kiểm tra token
        assertTrue(jwtResponse.getRoles().contains("ROLE_RENTAL")); // Kiểm tra quyền
    }
    /**
     * Test case: Đăng nhập thất bại do không có quyền RENTAL
     */
    @Test
    void loginRental_NotRentalRole() {
        // Arrange: Chuẩn bị dữ liệu và mock các hành vi
        authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")); // Thay đổi quyền thành USER
        doReturn(authorities).when(userDetails).getAuthorities();  // Mock quyền USER
        when(authentication.getPrincipal()).thenReturn(userDetails); // Mock thông tin người dùng
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);                       // Mock quá trình xác thực thành công

        // Act: Thực hiện phương thức cần test
        ResponseEntity<?> response = authController.authenticatedRenter(loginRequest);

        // Assert: Kiểm tra kết quả
        assertNotNull(response);                                  // Kiểm tra response không null
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode()); // Kiểm tra status code 403
        assertEquals("Chỉ rental mới có quyền truy cập.", response.getBody()); // Kiểm tra thông báo lỗi
    }

    /**
     * Test case: Đăng nhập thất bại do tài khoản chưa được xác nhận
     */
    @Test
    void loginRental_NotApproved() {
        // Arrange: Chuẩn bị dữ liệu và mock các hành vi
        doReturn(authorities).when(userDetails).getAuthorities();  // Mock quyền RENTAL
        doReturn(false).when(userDetails).isApproved();           // Mock trạng thái chưa xác nhận
        when(authentication.getPrincipal()).thenReturn(userDetails); // Mock thông tin người dùng
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);                       // Mock quá trình xác thực thành công

        // Act: Thực hiện phương thức cần test
        ResponseEntity<?> response = authController.authenticatedRenter(loginRequest);

        // Assert: Kiểm tra kết quả
        assertNotNull(response);                                  // Kiểm tra response không null
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode()); // Kiểm tra status code 403
        assertEquals("Tài khoản rental chưa được xác nhận.", response.getBody()); // Kiểm tra thông báo lỗi
    }
}
