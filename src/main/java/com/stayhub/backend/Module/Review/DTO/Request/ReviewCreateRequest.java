package com.stayhub.backend.Module.Review.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReviewCreateRequest(
        @Schema(description = "Số sao đánh giá từ 1 đến 5", example = "5")
        @NotNull(message = "Số sao đánh giá không được để trống")
        @Min(value = 1, message = "Đánh giá thấp nhất là 1 sao")
        @Max(value = 5, message = "Đánh giá cao nhất là 5 sao")
        Integer rating,

        @Schema(description = "Bình luận đánh giá", example = "Căn hộ rất đẹp và sạch sẽ, tôi rất hài lòng!")
        String comment,

        @Schema(description = "Danh sách URL hình ảnh đánh giá", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
        List<String> imageUrls
) {
}
