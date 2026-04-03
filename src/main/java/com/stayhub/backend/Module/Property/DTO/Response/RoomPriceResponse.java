package com.stayhub.backend.Module.Property.DTO.Response;

import java.math.BigDecimal;
import java.util.List;

public record RoomPriceResponse(
        Long roomId,
        BigDecimal calculatedTotalPrice,
        List<DailyPriceDTO> priceBreakdown
) {
}
