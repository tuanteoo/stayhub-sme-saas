package com.stayhub.backend.Module.Property.Service.Implement;

import com.stayhub.backend.Common.Mapper.CancellationPolicyMapper;
import com.stayhub.backend.Module.Property.DTO.Response.CancellationPolicyResponse;
import com.stayhub.backend.Module.Property.Repository.CancellationPolicyRepository;
import com.stayhub.backend.Module.Property.Service.CancellationPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CancellationPolicyServiceImpl implements CancellationPolicyService {
    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final CancellationPolicyMapper cancellationPolicyMapper;

    @Override
    public List<CancellationPolicyResponse> getActivePolicies() {
        return cancellationPolicyRepository.findByIsActiveTrue().stream()
                .map(cancellationPolicyMapper::toResponse)
                .collect(Collectors.toList());
    }
}
