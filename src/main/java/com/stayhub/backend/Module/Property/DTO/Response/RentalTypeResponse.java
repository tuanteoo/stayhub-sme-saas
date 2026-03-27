package com.stayhub.backend.Module.Property.DTO.Response;

import lombok.Builder;

import java.util.List;

@Builder
public record RentalTypeResponse(
        Long id,
        String name,
        String slug,
        String description,
        String iconName,
        List<CategoryResponse> categoryResponses
) {
}
