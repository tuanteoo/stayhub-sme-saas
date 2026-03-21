package com.stayhub.backend.Module.Property.DTO.Request;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank(message = "Tên danh mục không được để trống") String name,
        String description,
        @NotBlank(message = "Tên icon không được để trống") String iconName,
        Boolean isActive
) {
}
