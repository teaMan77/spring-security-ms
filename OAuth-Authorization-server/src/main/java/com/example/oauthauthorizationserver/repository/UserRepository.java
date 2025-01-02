package com.example.oauthauthorizationserver.repository;

import com.example.oauthauthorizationserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByEmail(String email);
}
