package com.stayhub.backend.Module.Identity.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record HostVerificationRequest(

        @Schema(description = "Số điện thoại kinh doanh của chủ nhà", example = "0123456789")
        @NotBlank(message = "Số điện thoại kinh doanh không được để trống")
        @Pattern(regexp = "^(0|84|\\+84)(3[2-9]|5[2689]|7[06-9]|8[1-9]|9[0-9])([0-9]{7})$", message = "Số điện thoại không đúng định dạng")
        String businessPhone,

        @Schema(description = "Email hỗ trợ của chủ nhà", example = "support@stayhub.com")
        @NotBlank(message = "Email hỗ trợ không được để trống")
        @Email(message = "Định dạng email không hợp lệ")
        String supportEmail,

        @Schema(description = "CCCD/Định danh của chủ nhà", example = "123456789012")
        @NotBlank(message = "Số CCCD/Định danh không được để trống")
        @Pattern(regexp = "^[0-9]{12}$", message = "Căn cước công dân phải bao gồm đúng 12 chữ số")
        String identityCardNumber,

        @NotBlank(message = "Ảnh mặt trước CCCD không được để trống")
        String identityCardFrontUrl,

        @NotBlank(message = "Ảnh mặt sau CCCD không được để trống")
        String identityCardBackUrl,

        @Schema(description = "Mã số giấy phép kinh doanh của chủ nhà", example = "1234567890")
        @Size(max = 50, message = "Mã số giấy phép kinh doanh không được vượt quá 50 ký tự")
        String businessLicenseNumber,

        String businessLicenseUrl
) {
}
