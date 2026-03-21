package com.stayhub.backend.Module.Property.DTO.Request;

import com.stayhub.backend.Common.Util.AmenityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AmenityRequest(
        @NotBlank(message = "Tên tiện ích không được để trống") String name,
        @NotBlank(message = "Tên icon không được để trống") String iconName,
        @NotNull(message = "Loại tiện ích không được để trống") AmenityType type
) {
}
