package com.stayhub.backend.Module.Identity.Service;

import com.stayhub.backend.Module.Identity.DTO.Request.HostApprovalRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.HostRegistrationWithPropertyRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.HostVerificationRequest;

public interface HostOnboardingService {
    String submitHostApplication(String email, HostRegistrationWithPropertyRequest request);
    void reviewHostApplication(Long id, HostApprovalRequest request);
}
