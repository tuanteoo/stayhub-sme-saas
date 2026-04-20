package com.stayhub.backend.Module.Identity.DTO.Response;

import com.stayhub.backend.Common.Util.Gender;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record UserProfileResponse(
        Long id,
        String email,
        String fullName,
        String avatarUrl,
        String phoneNumber,
        Gender gender,
        LocalDate dateOfBirth,
        String addressDetail,
        String bio,

        String hostCode,
        String businessPhone,
        String supportEmail,
        String onboardingStatus,
        String maskedIdentityCard,
        String maskedBusinessLicense,
        LocalDateTime joinedAt
) {
}
