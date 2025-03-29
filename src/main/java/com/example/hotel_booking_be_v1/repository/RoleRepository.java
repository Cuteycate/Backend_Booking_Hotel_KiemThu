package com.example.hotel_booking_be_v1.repository;

import com.example.hotel_booking_be_v1.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByName(String role);
    boolean existsByName(String role);

}
