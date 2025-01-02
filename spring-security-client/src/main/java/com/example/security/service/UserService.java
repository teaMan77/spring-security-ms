package com.example.security.service;

import com.example.security.entity.User;
import com.example.security.entity.VerificationToken;
import com.example.security.model.PasswordModel;
import com.example.security.model.UserModel;

public interface UserService {
    User registerUser(UserModel userModel);

    void saveVerificationTokenForUser(User user, String token);

    String validateVerificationToken(String token);

    VerificationToken resendNewVerificationToken(String oldToken);

    User getUserByEmail(String email);

    void createPasswordToken(User user, String token);

    String validatePasswordToken(String token, PasswordModel passwordModel);

    String validateAndChangePassword(User user, PasswordModel passwordModel);
}
