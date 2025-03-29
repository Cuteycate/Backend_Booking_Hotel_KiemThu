package com.example.hotel_booking_be_v1.security;

import com.example.hotel_booking_be_v1.model.Role;
import com.example.hotel_booking_be_v1.model.User;
import com.example.hotel_booking_be_v1.repository.RoleRepository;
import com.example.hotel_booking_be_v1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;
import java.util.Set;

@Configuration
@EnableMethodSecurity(securedEnabled = true,jsr250Enabled = true)
@RequiredArgsConstructor

public class WebSecurityConfig {
    private final HotelUserDetailsService userDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils; // Thêm dòng này
//    @Autowired
//    private PasswordEncoder passwordEncoder; // Tiêm PasswordEncoder qua @Autowired
//    private final PasswordEncoder passwordEncoder; // Thêm dòng này
    @Bean
    public AuthTokenFilter authenticationTokenFilter(){
        return new AuthTokenFilter();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception{
        return authConfig.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/auth/google/**","/rooms/**","/bookings/**", "/login-rental",
                                "/api/bookings/**", "/api/rental/register", "/api/search/autocomplete", "/api/hotels/**",
                        "/hotels/by-ward/**", "/hotels/by-district/**", "/hotels/by-province/**", "/hotels/hotels/**",
                                "/reviews/hotel/**")
                        .permitAll()
                        .requestMatchers("/roles/**","/api/rental/approve/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated())
                .oauth2Login() // Thêm cấu hình OAuth2 login
                .loginPage("/login") // Tùy chọn trang login tùy chỉnh
                .defaultSuccessUrl("/home", true);
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
//@Bean
//public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//    http.csrf(AbstractHttpConfigurer::disable)
//            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint))
//            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//            .authorizeHttpRequests(auth -> auth
//                    .requestMatchers("/auth/**", "/rooms/**", "/bookings/**").permitAll()
//                    .requestMatchers("/roles/**").hasRole("ADMIN")
//                    .anyRequest().authenticated()
//            )
//            .oauth2Login(oauth2 -> oauth2
//                    .loginPage("/login")
//                    .successHandler((request, response, authentication) -> {
//                        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
//                        Map<String, Object> attributes = token.getPrincipal().getAttributes();
//
//                        String email = (String) attributes.get("email");
//                        String name = (String) attributes.get("name");
//
//                        // Kiểm tra xem user đã tồn tại chưa
//                        if (!userRepository.existsByEmail(email)) {
//                            User newUser = new User();
//                            newUser.setEmail(email);
//                            newUser.setFirstName(name);
//                            Role role = roleRepository.findByName("ROLE_USER")
//                                    .orElseThrow(() -> new RuntimeException("Role not found"));
//                            newUser.setRoles(Set.of(role));
//                            newUser.setPassword(passwordEncoder.encode("google_oauth")); // Mật khẩu giả
//                            userRepository.save(newUser);
//                        }
//
//                        // Tạo JWT
//                        String jwt = jwtUtils.generateJwtTokenForGoogle(attributes);
//                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                        response.getWriter().write("{\"token\": \"" + jwt + "\"}");
//                    })
//
//            );
//
//    http.authenticationProvider(authenticationProvider());
//    http.addFilterBefore(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
//    return http.build();
//}



}
