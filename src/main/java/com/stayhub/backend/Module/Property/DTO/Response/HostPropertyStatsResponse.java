package com.stayhub.backend.Module.Property.DTO.Response;

import lombok.Builder;

@Builder
public record HostPropertyStatsResponse(
        long totalProperties,
        long draftCount,
        long pendingReviewCount,
        long activeCount,
        long inactiveCount,
        long rejectedCount,
        long hiddenCount,
        long bannedCount,

        Integer maxListingsAllowed,
        boolean canCreateNewProperty
) {
}
