package com.stayhub.backend.Module.Identity.DTO.Response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record UserAdminResponse(
        Long id,
        String email,
        String fullName,
        String avatarUrl,
        String phoneNumber,
        List<String> roles,
        String status,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
}
