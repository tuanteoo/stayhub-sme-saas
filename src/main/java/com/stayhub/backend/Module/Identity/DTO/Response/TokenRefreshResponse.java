package com.stayhub.backend.Module.Identity.DTO.Response;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken
) {
}
