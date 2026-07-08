package com.stayhub.backend.Module.Property.DTO.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import com.stayhub.backend.Common.Util.PropertyStatus;

@Builder
public record PropertyApprovalRequest(
        @NotNull(message = "Trạng thái cập nhật không được để trống")
        PropertyStatus status,
        String reason
) {
}
