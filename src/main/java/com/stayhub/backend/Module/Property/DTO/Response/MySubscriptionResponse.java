package com.stayhub.backend.Module.Property.DTO.Response;

import com.stayhub.backend.Common.Util.SubscriptionTier;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MySubscriptionResponse(
        Long subscriptionId,
        SubscriptionTier tier,
        String planName,
        Integer maxListings,
        Double commissionRate,
        BigDecimal creditLimit,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String status
) {
}
