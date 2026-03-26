package com.stayhub.backend.Module.Identity.DTO.Response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HostInfoResponse(
        Long id,
        String hostCode,
        String fullName,
        String avatarUrl,
        LocalDateTime joinedAt
) {
}
