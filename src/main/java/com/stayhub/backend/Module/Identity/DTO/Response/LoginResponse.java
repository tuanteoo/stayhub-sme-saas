package com.stayhub.backend.Module.Identity.DTO.Response;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        List<String> roles,
        String status,
        UserProfileResponse userInfResponse
) { }
