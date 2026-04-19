package com.stayhub.backend.Module.Property.DTO.Response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminPropertyResponse(
        Long id,
        String thumbnailUrl,
        String name,
        String slug,
        String hostName,
        String hostAvatarUrl,
        String hostEmail,
        String categoryName,
        String province,
        String district,
        String status,
        LocalDateTime createdAt
) {
}
