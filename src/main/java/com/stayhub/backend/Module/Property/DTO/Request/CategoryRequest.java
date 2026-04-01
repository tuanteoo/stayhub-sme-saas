package com.stayhub.backend.Module.Property.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Tên danh mục không được để trống")
        @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
        String name,

        @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
        String description,

        @NotBlank(message = "Tên biểu tượng không được để trống")
        @Size(max = 255, message = "Tên biểu tượng không được vượt quá 255 ký tự")
        String iconName
) {
}
