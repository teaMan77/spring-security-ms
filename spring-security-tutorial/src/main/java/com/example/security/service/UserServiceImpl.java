package com.example.security.service;

import com.example.security.entity.PasswordToken;
import com.example.security.entity.User;
import com.example.security.entity.VerificationToken;
import com.example.security.model.PasswordModel;
import com.example.security.model.UserModel;
import com.example.security.repository.PasswordTokenRepository;
import com.example.security.repository.UserRepository;
import com.example.security.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Calendar;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordTokenRepository passwordTokenRepository;

    @Override
    public User registerUser(UserModel userModel) {
        User user = new User();
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setEmail(userModel.getEmail());
        user.setRole("USER");

        if (userModel.getPassword().equals(userModel.getMatchingPassword())) {
            user.setPassword(passwordEncoder.encode(userModel.getPassword()));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please enter the correct password for matching password..." );
        }

        userRepository.save(user);
        return user;
    }

    @Override
    public void saveVerificationTokenForUser(User user, String token) {
        VerificationToken verificationToken = new VerificationToken(user, token);
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public String validateVerificationToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            return "Invalid User Token";
        }

        User user = verificationToken.getUser();
        Calendar calendar = Calendar.getInstance();

        if (verificationToken.getExpirationTime().getTime() - calendar.getTime().getTime() <= 0) {
            verificationTokenRepository.delete(verificationToken);
            return "Token Expired";
        }

        user.setEnabled(true);
        userRepository.save(user);

        return "valid";
    }

    @Override
    public VerificationToken resendNewVerificationToken(String oldToken) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(oldToken);
        if (verificationToken == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Token...");
        }

        String newToken = UUID.randomUUID().toString();
        verificationToken.setToken(newToken);

        return verificationTokenRepository.save(verificationToken);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public void createPasswordToken(User user, String token) {
        PasswordToken passwordToken = passwordTokenRepository.findByUserId(user.getId());

        if (passwordToken != null) {
            passwordTokenRepository.delete(passwordToken);
            PasswordToken newPasswordToken = new PasswordToken(user, token);
            passwordTokenRepository.save(newPasswordToken);
        } else {
            PasswordToken newPasswordToken = new PasswordToken(user, token);
            passwordTokenRepository.save(newPasswordToken);
        }
    }

    @Override
    @Transactional
    public String validatePasswordToken(String token, PasswordModel passwordModel) {
        PasswordToken passwordToken = passwordTokenRepository.findByToken(token);

        if (passwordToken == null) {
            return "Invalid User Token";
        }

        User user = passwordToken.getUser();
        Calendar calendar = Calendar.getInstance();

        if (passwordToken.getExpirationTime().getTime() - calendar.getTime().getTime() <= 0) {
            passwordTokenRepository.delete(passwordToken);
            return "Token Expired";
        }

        if (!passwordToken.isUsed()) {
            user.setPassword(passwordEncoder.encode(passwordModel.getNewPassword()));
            passwordToken.setUsed(true);
            userRepository.save(user);
            return "valid";
        } else {
            return "Link Expired...";
        }

    }

    @Override
    public String validateAndChangePassword(User user, PasswordModel passwordModel) {
        if (passwordEncoder.matches(passwordModel.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(passwordModel.getNewPassword()));
            userRepository.save(user);
            return "Password changed successfully...";
        }

        return "Incorrect old password...";
    }
}
