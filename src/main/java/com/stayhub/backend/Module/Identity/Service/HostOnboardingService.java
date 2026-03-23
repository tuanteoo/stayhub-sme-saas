package com.stayhub.backend.Module.Identity.Service;

import com.stayhub.backend.Module.Identity.DTO.Request.HostVerificationRequest;

public interface HostOnboardingService {
    void submitHostApplication(String email, HostVerificationRequest request);
}
