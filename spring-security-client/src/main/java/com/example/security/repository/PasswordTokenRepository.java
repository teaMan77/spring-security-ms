package com.example.security.repository;

import com.example.security.entity.PasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordTokenRepository extends JpaRepository<PasswordToken, Long> {
    PasswordToken findByToken(String token);

    PasswordToken findByUserId(Long id);
}
