package com.stayhub.backend.Module.Property.DTO.Response;

import com.stayhub.backend.Common.Util.SubscriptionTier;
import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record SubscriptionPlanResponse(
        Long id,
        String name,
        String description,
        SubscriptionTier tier,
        BigDecimal price,
        Integer durationMonths,
        Integer maxListings,
        Double commissionRate,
        BigDecimal creditLimit
) {
}
