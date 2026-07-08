package com.stayhub.backend.Module.Property.DTO.Response;

import java.math.BigDecimal;
import java.util.List;

public record PropertyCardResponse(
        Long id,
        String name,
        String slug,
        String province,
        String district,
        BigDecimal pricePerNight,
        String thumbnailUrl,
        Double ratingAvg,
        Integer maxGuests,
        Integer numBedrooms,
        Integer numBeds,
        Integer numBathrooms,
        List<String> amenities
) {
}
