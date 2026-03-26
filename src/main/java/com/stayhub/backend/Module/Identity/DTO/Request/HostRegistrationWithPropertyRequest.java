package com.stayhub.backend.Module.Identity.DTO.Request;

import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record HostRegistrationWithPropertyRequest(
        @Valid
        @NotNull(message = "Thông tin hồ sơ Chủ nhà không được để trống")
        HostVerificationRequest hostDetails,

        @Valid
        @NotNull(message = "Thông tin bài đăng nhà đầu tiên không được để trống")
        PropertyCreateRequest firstProperty
) {
}
