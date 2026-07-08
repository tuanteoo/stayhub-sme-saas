package com.stayhub.backend.Module.Property.DTO.Response;

import lombok.Builder;

@Builder
public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        String iconName
) {
}
