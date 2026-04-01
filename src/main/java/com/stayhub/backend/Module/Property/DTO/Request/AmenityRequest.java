package com.stayhub.backend.Module.Property.DTO.Request;

import com.stayhub.backend.Common.Util.AmenityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AmenityRequest(
        @NotBlank(message = "Tên tiện ích không được để trống")
        @Size(max = 100, message = "Tên tiện ích không được vượt quá 100 ký tự")
        String name,

        @NotBlank(message = "Tên biểu tượng không được để trống")
        @Size(max = 255, message = "Tên biểu tượng không được vượt quá 255 ký tự")
        String iconName,

        @NotNull(message = "Loại tiện ích không được để trống")
        AmenityType type
) {
}
