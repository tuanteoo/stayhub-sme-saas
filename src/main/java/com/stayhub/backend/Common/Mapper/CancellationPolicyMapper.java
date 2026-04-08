package com.stayhub.backend.Common.Mapper;

import com.stayhub.backend.Module.Property.DTO.Response.CancellationPolicyResponse;
import com.stayhub.backend.Module.Property.Model.CancellationPolicy;
import org.springframework.stereotype.Component;

@Component
public class CancellationPolicyMapper {
    public CancellationPolicyResponse toResponse(CancellationPolicy policy) {
        if (policy == null) return null;
        return CancellationPolicyResponse.builder()
                .id(policy.getId())
                .name(policy.getName())
                .description(policy.getDescription())
                .refundPercentage(policy.getRefundPercentage())
                .daysBeforeCheckin(policy.getDaysBeforeCheckin())
                .build();
    }
}
