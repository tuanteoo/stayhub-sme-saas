package com.stayhub.backend.Module.Identity.DTO.Response;

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
