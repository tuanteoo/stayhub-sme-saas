package com.stayhub.backend.Module.Identity.Service;

import com.stayhub.backend.Module.Identity.DTO.Request.HostApprovalRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.HostRegistrationWithPropertyRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.HostVerificationRequest;

public interface HostOnboardingService {
    String submitHostApplication(Long hostId, HostRegistrationWithPropertyRequest request);
    void reviewHostApplication(String hostCode, HostApprovalRequest request);
}
