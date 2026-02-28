package com.stayhub.backend.Module.Identity.Service;

import org.springframework.stereotype.Service;

public interface EmailService {
    void sendVerificationEmailAsync(String toEmail, String fullName, String token);
}
