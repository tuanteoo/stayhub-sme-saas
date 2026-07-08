package com.stayhub.backend.Module.Identity.DTO.Response;

import com.stayhub.backend.Module.Property.DTO.Response.PropertyDetailResponse;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HostApplicationDetailResponse(
        Long hostId,
        String hostCode,
        String fullName,
        String hostAvatarUrl,
        String email,
        String businessPhone,
        String supportEmail,
        String identityCardNumber,
        String identityCardFrontUrl,
        String identityCardBackUrl,
        String businessLicenseNumber,
        String businessLicenseUrl,
        String onboardingStatus,
        String reviewNote,
        LocalDateTime createdAt,
        PropertyDetailResponse propertyDetailResponse
) {
}
