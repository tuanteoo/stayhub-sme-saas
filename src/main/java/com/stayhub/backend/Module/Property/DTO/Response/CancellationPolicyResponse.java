package com.stayhub.backend.Module.Property.DTO.Response;

import lombok.Builder;

@Builder
public record CancellationPolicyResponse(
        Long id,
        String name,
        String description,
        Integer refundPercentage,
        Integer daysBeforeCheckin
) {
}
