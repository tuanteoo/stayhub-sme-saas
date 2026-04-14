package com.stayhub.backend.Module.Identity.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @Schema(description = "Refresh Token dùng để đăng xuất khỏi hệ thống")
        @NotBlank(message = "Refresh Token không được để trống")
        String refreshToken
)
{ }
