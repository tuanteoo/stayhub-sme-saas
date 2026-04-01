package com.stayhub.backend.Module.Property.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RoomResponse(
        Long id,
        String name,
        String description,
        BigDecimal pricePerNight,
        Integer maxGuests,
        Integer numBeds,
        Integer numBathrooms,
        List<AmenityResponse> amenities,
        String thumbnailUrl,
        CancellationPolicyResponse cancellationPolicyResponse,
        List<LocalDate> blockedDates
) {
}
