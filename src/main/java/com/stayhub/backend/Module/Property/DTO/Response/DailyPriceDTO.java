package com.stayhub.backend.Module.Property.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyPriceDTO(
        LocalDate date,
        BigDecimal price
) {
}
