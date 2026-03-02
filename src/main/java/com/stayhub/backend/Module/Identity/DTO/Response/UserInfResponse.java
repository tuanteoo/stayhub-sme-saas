package com.stayhub.backend.Module.Identity.DTO.Response;

public record UserInfResponse(
        String email,
        String fullName,
        String avatarUrl
) {
}
