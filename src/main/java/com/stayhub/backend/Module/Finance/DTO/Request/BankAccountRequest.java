package com.stayhub.backend.Module.Finance.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record BankAccountRequest(

        @Schema(description = "Mã ngân hàng", example = "VCB")
        @NotBlank(message = "Mã ngân hàng không được để trống")
        @Pattern(regexp = "^[A-Z0-9]+$", message = "Mã ngân hàng chỉ chứa chữ IN HOA và số, không có khoảng trắng")
        String bankCode,

        @Schema(description = "Số tài khoản", example = "123456789")
        @Pattern(regexp = "^[0-9]{6,20}$", message = "Số tài khoản không hợp lệ (Chỉ chứa số, từ 6-20 ký tự)")
        @NotBlank(message = "Số tài khoản không được để trống")
        String accountNumber,

        @Schema(description = "Tên chủ tài khoản", example = "Nguyen Van A")
        @NotBlank(message = "Tên chủ tài khoản không được để trống")
        @Pattern(regexp = "^[A-Z\\s]+$", message = "Tên chủ tài khoản phải VIẾT HOA, KHÔNG DẤU và không chứa ký tự đặc biệt")
        String accountHolderName,

        @Schema(description = "Chi nhánh ngân hàng", example = "Hà Nội")
        String branch,

        @Schema(description = "Đặt tài khoản này làm mặc định", example = "true")
        Boolean isDefault
) { }
