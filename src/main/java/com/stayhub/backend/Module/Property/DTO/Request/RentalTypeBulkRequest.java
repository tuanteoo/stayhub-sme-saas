package com.stayhub.backend.Module.Property.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RentalTypeBulkRequest(

        @Schema(description = "Tên loại hình cho thuê cơ sở lưu trú", example = "Ở chung phòng")
        @NotBlank(message = "Tên loại hình không được để trống")
        @Size(max = 100, message = "Tên loại hình không được vượt quá 100 ký tự")
        String name,

        @Schema(description = "Mô tả về loại hình cho thuê cơ sở lưu trú", example = "Loại hình ở chung phòng sẽ có giá rẻ hơn so với các loại hình khác")
        @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
        String description,

        @Schema(description = "Tên biểu tượng của loại hình cho thuê cơ sở lưu trú dùng cho FE", example = "shared-room")
        @NotBlank(message = "Tên biểu tượng không được để trống")
        @Size(max = 100, message = "Tên biểu tượng không được vượt quá 100 ký tự")
        String iconName,

        @Schema(description = "Danh sách các danh mục thuộc loại hình cho thuê cơ sở lưu trú")
        @Valid List<CategoryRequest> categories
) {
}
