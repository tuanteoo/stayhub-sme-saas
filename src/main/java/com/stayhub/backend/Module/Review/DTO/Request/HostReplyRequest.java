package com.stayhub.backend.Module.Review.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record HostReplyRequest(
        @Schema(description = "Nội dung phản hồi của chủ nhà", example = "Cảm ơn bạn đã đánh giá, chúng tôi rất vui khi biết bạn hài lòng với căn hộ!")
        @NotBlank(message = "Nội dung phản hồi không được để trống")
        String reply
) {
}
