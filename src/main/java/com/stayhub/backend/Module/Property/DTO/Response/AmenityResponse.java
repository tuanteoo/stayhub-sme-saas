package com.stayhub.backend.Module.Property.DTO.Response;

import com.stayhub.backend.Common.Util.AmenityType;
import lombok.Builder;

@Builder
public record AmenityResponse(
        Long id,
        String name,
        String iconName,
        AmenityType type
) {
}
