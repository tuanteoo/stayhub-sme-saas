package com.stayhub.backend.Module.Identity.Service;

import com.stayhub.backend.Module.Identity.DTO.Request.*;
import com.stayhub.backend.Module.Identity.DTO.Response.LoginResponse;
import com.stayhub.backend.Module.Identity.DTO.Response.TokenRefreshResponse;

public interface AuthService {
    String registerGuest(RegisterGuestRequest request);
    String verifyEmailToken(String token);
    LoginResponse login(LoginRequest request);
    void logout(LogoutRequest request);
    TokenRefreshResponse refreshToken(RefreshTokenRequest request);
    void processForgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
