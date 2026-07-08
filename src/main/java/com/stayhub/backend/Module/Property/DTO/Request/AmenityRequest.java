package com.stayhub.backend.Module.Property.DTO.Request;

import com.stayhub.backend.Common.Util.AmenityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AmenityRequest(
        @Schema(description = "Tên tiện ích", example = "Hồ bơi")
        @NotBlank(message = "Tên tiện ích không được để trống")
        @Size(max = 100, message = "Tên tiện ích không được vượt quá 100 ký tự")
        String name,

        @Schema(description = "Tên icon của tiện ích dùng cho FE", example = "pool")
        @NotBlank(message = "Tên biểu tượng không được để trống")
        @Size(max = 255, message = "Tên biểu tượng không được vượt quá 255 ký tự")
        String iconName,

        @Schema(description = "Loại tiện tích (FAVOURITE - OUTSTANDING - SAFETY)", example = "SAFETY")
        @NotNull(message = "Loại tiện ích không được để trống")
        AmenityType type
) {
}
