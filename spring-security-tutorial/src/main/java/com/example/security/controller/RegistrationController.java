package com.example.security.controller;

import com.example.security.entity.PasswordToken;
import com.example.security.entity.User;
import com.example.security.entity.VerificationToken;
import com.example.security.event.RegistrationCompleteEvent;
import com.example.security.model.PasswordModel;
import com.example.security.model.UserModel;
import com.example.security.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@Slf4j
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private ApplicationEventPublisher publisher;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {
        User user = userService.registerUser(userModel);
        publisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request)));
        return ResponseEntity.ok("User Registered Successfully...");
    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token) {
        String result = userService.validateVerificationToken(token);
        if (result.equalsIgnoreCase("valid")) {
            return "User Verified Successfully...";
        }
        return result;
    }

    @GetMapping("/resendVerificationToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken, HttpServletRequest request) {
        VerificationToken newVerificationToken = userService.resendNewVerificationToken(oldToken);
        resendVerificationMail(newVerificationToken, applicationUrl(request));
        return "New Token Sent Successfully...";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request) {
        User user = userService.getUserByEmail(passwordModel.getEmail());

        if (user != null) {
            String token = UUID.randomUUID().toString();
            userService.createPasswordToken(user, token);

            sendResetPasswordMail(token, applicationUrl(request));
            return "Reset Password Link Sent Successfully...";
        }
        return "User Not Found...";
    }

    @PostMapping("/saveNewPassword")
    public String setNewPassword(@RequestParam("token") String token, @RequestBody PasswordModel passwordModel) {
        if (passwordModel.getNewPassword().equals(passwordModel.getConfirmPassword())) {
            String result = userService.validatePasswordToken(token, passwordModel);

            if (result.equalsIgnoreCase("valid"))
                return "Password Reset Successfully...";
            else
                return result;
        }

        return "New password must match with the confirm password...";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel) {
        if (passwordModel.getNewPassword().equals(passwordModel.getConfirmPassword())) {
            User user = userService.getUserByEmail(passwordModel.getEmail());

            if (user == null) {
                return "User not found...";
            }

            return userService.validateAndChangePassword(user, passwordModel);
        }

        return "New password must match with the confirm password";

    }

    private void sendResetPasswordMail(String token, String applicationUrl) {
         String url = applicationUrl + "/saveNewPassword?token=" + token;
        //implement mailing functionality -- TO DO
        log.info("Please click the link to reset your password: {}", url);
    }

    private void resendVerificationMail(VerificationToken newVerificationToken, String applicationUrl) {
        String url = applicationUrl + "/verifyRegistration?token=" + newVerificationToken.getToken();
        //implement mailing functionality -- TO DO
        log.info("Please click the link to verify your account: {}", url);
    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
