package com.stayhub.backend.Module.Identity.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record UpdateHostProfileRequest(
        @Schema(description = "Số điện thoại kinh doanh, bắt đầu bằng 0, 84 hoặc +84, theo sau là 9 chữ số", example = "0912345678")
        @Pattern(regexp = "^(0|84|\\+84)(3[2-9]|5[2689]|7[06-9]|8[1-9]|9[0-9])([0-9]{7})$", message = "Số điện thoại không đúng định dạng")
        String businessPhone,

        @Email(message = "Email hỗ trợ không hợp lệ")
        String supportEmail,

        @Valid
        UpdateUserProfileRequest updateProfileRequest
) {
}
