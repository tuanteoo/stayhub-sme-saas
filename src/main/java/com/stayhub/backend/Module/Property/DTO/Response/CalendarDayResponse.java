package com.stayhub.backend.Module.Property.DTO.Response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CalendarDayResponse(
        LocalDate date,
        BigDecimal price,
        Boolean isLocked,
        Boolean isSoldOut
) {
}
