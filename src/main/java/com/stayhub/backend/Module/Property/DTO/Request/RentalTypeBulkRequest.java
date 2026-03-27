package com.stayhub.backend.Module.Property.DTO.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RentalTypeBulkRequest(
        @NotBlank(message = "Tên loại hình không được để trống")
        String name,

        String description,

        @NotBlank(message = "Tên icon không được để trống")
        String iconName,

        @Valid List<CategoryRequest> categories
) {
}
