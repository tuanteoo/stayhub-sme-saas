package com.stayhub.backend.Module.Property.Service;

import com.stayhub.backend.Module.Property.DTO.Response.CancellationPolicyResponse;

import java.util.List;

public interface CancellationPolicyService {
    List<CancellationPolicyResponse> getActivePolicies();
}
