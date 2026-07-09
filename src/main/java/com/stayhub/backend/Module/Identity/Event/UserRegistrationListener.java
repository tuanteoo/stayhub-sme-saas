package com.stayhub.backend.Module.Identity.Event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserRegistrationListener {

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Observer Pattern: Received UserRegisteredEvent for email: {}", event.getUser().getEmail());
        // Logic such as sending welcome email or initial setup can be moved here
    }
}
