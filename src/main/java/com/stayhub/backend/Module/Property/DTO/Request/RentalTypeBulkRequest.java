package com.stayhub.backend.Module.Property.DTO.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RentalTypeBulkRequest(
        @NotBlank(message = "Tên loại hình không được để trống")
        @Size(max = 100, message = "Tên loại hình không được vượt quá 100 ký tự")
        String name,

        @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
        String description,

        @NotBlank(message = "Tên biểu tượng không được để trống")
        @Size(max = 100, message = "Tên biểu tượng không được vượt quá 100 ký tự")
        String iconName,

        @Valid List<CategoryRequest> categories
) {
}
