package com.stayhub.backend.Module.Booking.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record DisputeCreateRequest(

        @Schema(description = "Lý do tạo tranh chấp", example = "Phòng không sạch sẽ")
        @NotNull
        String reason,

        @Schema(description = "Mô tả chi tiết về tranh chấp", example = "Phòng có mùi hôi và không được dọn dẹp trước khi tôi đến.")
        String description,

        @Schema(description = "URL hình ảnh minh chứng khiếu nại", example = "http://example.com/image1.jpg")
        String evidenceImageUrls
) {
}
