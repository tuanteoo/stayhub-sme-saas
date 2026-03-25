package com.stayhub.backend.Module.Identity.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record HostVerificationRequest(
        @NotBlank(message = "Số điện thoại kinh doanh không được để trống")
        String businessPhone,

        @NotBlank(message = "Email hỗ trợ không được để trống")
        @Email
        String supportEmail,

        @NotBlank(message = "Số CCCD/Định danh không được để trống")
        String identityCardNumber,

        @NotBlank(message = "Ảnh mặt trước CCCD không được để trống")
        String identityCardFrontUrl,

        @NotBlank(message = "Ảnh mặt sau CCCD không được để trống")
        String identityCardBackUrl,

        String businessLicenseNumber,
        String businessLicenseUrl
) {
}
