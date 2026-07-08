package com.stayhub.backend.Module.Identity.DTO.Request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh Token không được để trống")
        String refreshToken
) {
}
