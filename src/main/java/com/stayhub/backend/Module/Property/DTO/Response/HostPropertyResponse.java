package com.stayhub.backend.Module.Property.DTO.Response;
import com.stayhub.backend.Common.Util.PropertyStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HostPropertyResponse(
        Long id,
        String name,
        String slug,
        String addressDetail,
        String province,
        BigDecimal startingPrice,
        String thumbnailUrl,
        PropertyStatus status,
        Double ratingAvg,
        Integer reviewCount,
        LocalDateTime createdAt
) {
}
