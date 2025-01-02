package com.example.security.listener;

import com.example.security.entity.User;
import com.example.security.event.RegistrationCompleteEvent;
import com.example.security.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RegistrationCompleteEventListener implements
        ApplicationListener<RegistrationCompleteEvent> {

    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        //create the user verification token with link.
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        userService.saveVerificationTokenForUser(user, token);


        String url = event.getApplicationUrl() + "/verifyRegistration?token=" + token;

        //send the mail to user. --- TO DO
        log.info("Please click the link to verify your account: {}", url);
    }
}
