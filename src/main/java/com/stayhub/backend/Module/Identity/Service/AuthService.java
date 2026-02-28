package com.stayhub.backend.Module.Identity.Service;

import com.stayhub.backend.Module.Identity.DTO.Request.RegisterGuestRequest;

public interface AuthService {
    String registerGuest(RegisterGuestRequest request);
    String verifyEmailToken(String token);
}
