package com.stayhub.backend.Module.Property.DTO.Request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SubscriptionPlanRequest(
        @NotBlank(message = "Cấp độ (Tier) không được để trống")
        String tier,

        @NotBlank(message = "Tên gói cước không được để trống")
        String name,

        @NotNull(message = "Mô tả gói cước không được để trống")
        String description,

        @NotNull(message = "Giá gói cước không được để trống")
        @Min(value = 0, message = "Giá gói cước không hợp lệ")
        BigDecimal price,

        @NotNull(message = "Thời hạn gói cước (tháng) không được để trống")
        @Min(value = 1, message = "Thời hạn phải ít nhất là 1 tháng")
        Integer durationMonths,

        @Schema(description = "Số lượng bài đăng tối đa mà chủ nhà có thể tạo (null là không giới hạn)")
        Integer maxListings,

        @NotNull(message = "Tỷ lệ hoa hồng không được để trống")
        BigDecimal commissionRate,

        @NotNull(message = "Giới hạn tín dụng không được để trống")
        BigDecimal creditLimit
) {
}
