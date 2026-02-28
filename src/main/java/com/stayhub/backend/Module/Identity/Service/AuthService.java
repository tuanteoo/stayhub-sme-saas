package com.stayhub.backend.Module.Identity.Service;

import com.stayhub.backend.Module.Identity.DTO.Request.LoginRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.RegisterGuestRequest;
import com.stayhub.backend.Module.Identity.DTO.Response.LoginResponse;

public interface AuthService {
    String registerGuest(RegisterGuestRequest request);
    String verifyEmailToken(String token);
    LoginResponse login(LoginRequest request);
}
