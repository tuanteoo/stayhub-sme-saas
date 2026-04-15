package com.stayhub.backend.Module.Property.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import com.stayhub.backend.Common.Util.PropertyStatus;

@Builder
public record PropertyApprovalRequest(
        @Schema(description = "Trạng thái cập nhật của bài đăng", example = "PUBLISHED")
        @NotNull(message = "Trạng thái cập nhật không được để trống")
        PropertyStatus status
) {
}
