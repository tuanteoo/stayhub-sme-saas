package com.stayhub.backend.Module.Property.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CalendarUpdateRequest(
        @Schema(description = "Ngày bắt đầu, type: LocalDate, format: yyyy-MM-dd", example = "2026-05-01")
        LocalDate startDate,
        @Schema(description = "Ngày kết thúc, type: LocalDate, format: yyyy-MM-dd", example = "2026-05-06")
        LocalDate endDate,

        @Schema(description = "Danh sách ngày rời rạc, type: LocalDate, format: yyyy-MM-dd", example = "2026-05-01, 2026-05-03, 2026-05-05")
        List<LocalDate> dates,

        @Schema(description = "Giá thay đổi", example = "1000000")
        @DecimalMin(value = "0.00", message = "Giá thay đổi phải lớn hơn hoặc bằng 0.00")
        BigDecimal customPrice,

        @Schema(description = "Trạng thái khóa lịch, true nếu khóa, false nếu mở", example = "true")
        Boolean isLocked
) {
}
