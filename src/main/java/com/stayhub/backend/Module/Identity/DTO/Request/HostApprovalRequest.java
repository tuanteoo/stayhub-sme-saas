package com.stayhub.backend.Module.Identity.DTO.Request;

import com.stayhub.backend.Common.Util.HostOnboardingStatus;
import jakarta.validation.constraints.NotNull;

public record HostApprovalRequest(
        @NotNull(message = "Trạng thái phê duyệt không được để trống")
        HostOnboardingStatus status,

        String reviewNote
) {
}
