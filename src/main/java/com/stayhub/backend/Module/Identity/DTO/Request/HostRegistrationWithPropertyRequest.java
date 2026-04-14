package com.stayhub.backend.Module.Identity.DTO.Request;

import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record HostRegistrationWithPropertyRequest(
        @Schema(description = "Thông tin hồ sơ Chủ nhà")
        @Valid
        @NotNull(message = "Thông tin hồ sơ Chủ nhà không được để trống")
        HostVerificationRequest hostDetails,

        @Schema(description = "Thông tin bài đăng nhà đầu tiên của Chủ nhà")
        @Valid
        @NotNull(message = "Thông tin bài đăng nhà đầu tiên không được để trống")
        PropertyCreateRequest firstProperty
) {
}
