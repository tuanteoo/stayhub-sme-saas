package com.stayhub.backend.Module.Property.DTO.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record RoomCreateRequest(
        @NotBlank(message = "Tên phòng không được để trống") String name,
        String description,

        @NotNull(message = "Giá phòng không được để trống") BigDecimal pricePerNight,
        @NotNull @Min(1) Integer maxGuests,
        @NotNull @Min(1) Integer numBeds,
        @NotNull @Min(0) Integer numBathrooms,

        List<Long> amenityIds,
        List<String> imageUrls
) {
}
