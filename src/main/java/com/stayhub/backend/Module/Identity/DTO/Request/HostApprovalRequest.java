package com.stayhub.backend.Module.Identity.DTO.Request;

import com.stayhub.backend.Common.Util.HostOnboardingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HostApprovalRequest(
        @NotNull(message = "Trạng thái phê duyệt không được để trống")
        HostOnboardingStatus status,

        @Size(max = 1000, message = "Ghi chú phê duyệt không được vượt quá 1000 ký tự")
        String reviewNote
) {
}
