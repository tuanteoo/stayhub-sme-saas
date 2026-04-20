package com.stayhub.backend.Module.Identity.DTO.Request;

import com.stayhub.backend.Common.Util.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UpdateUserProfileRequest(
        @Size(min = 2, max = 100, message = "Họ và tên phải từ 2 đến 100 ký tự")
        String fullName,

        @Schema(description = "Số điện thoại kinh doanh, bắt đầu bằng 0, 84 hoặc +84, theo sau là 9 chữ số", example = "0912345678")
        @Pattern(regexp = "^(0|84|\\+84)(3[2-9]|5[2689]|7[06-9]|8[1-9]|9[0-9])([0-9]{7})$", message = "Số điện thoại không đúng định dạng")
        String phoneNumber,

        String avatarUrl,

        Gender gender,

        @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
        LocalDate dateOfBirth,

        String address,

        String bio
) {
}
