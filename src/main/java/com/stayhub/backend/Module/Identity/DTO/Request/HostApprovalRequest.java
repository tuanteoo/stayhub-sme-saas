package com.stayhub.backend.Module.Identity.DTO.Request;

import com.stayhub.backend.Common.Util.HostOnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HostApprovalRequest(

        @Schema(description = "Trạng thái phê duyệt của chủ nhà (APPROVED - REJECTED - REQUEST_CHANGES)", example = "APPROVED")
        @NotNull(message = "Trạng thái phê duyệt không được để trống")
        HostOnboardingStatus status,

        @Size(max = 1000, message = "Ghi chú phê duyệt không được vượt quá 1000 ký tự")
        String reviewNote
) {
}
