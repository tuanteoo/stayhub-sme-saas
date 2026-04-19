package com.stayhub.backend.Module.Identity.Service;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Module.Identity.DTO.Request.HostApprovalRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.HostRegistrationWithPropertyRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.HostVerificationRequest;
import com.stayhub.backend.Module.Identity.DTO.Response.HostApplicationDetailResponse;
import com.stayhub.backend.Module.Identity.DTO.Response.HostApplicationResponse;

public interface HostOnboardingService {
    String submitHostApplication(Long hostId, HostRegistrationWithPropertyRequest request);
    void reviewHostApplication(String hostCode, HostApprovalRequest request);
    PageResponse<HostApplicationResponse> getApplicationsForAdmin(String status, int page, int size, String sortBy, String sortDir);
    HostApplicationDetailResponse getApplicationDetailForAdmin(String hostCode);
}
