package com.stayhub.backend.Module.Booking.DTO.Request;

import com.stayhub.backend.Common.Util.BookingPaymentOption;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record BookingCreateRequest(

        @Schema(description = "ID căn nhà cần đặt - properties", example = "1")
        @NotNull(message = "ID căn nhà không được để trống")
        Long propertyId,

        @Schema(description = "Ngày nhận phòng, type: LocalDate, format: yyyy-MM-dd", example = "2026-10-10")
        @NotNull(message = "Ngày nhận phòng không được để trống")
        @FutureOrPresent(message = "Ngày nhận phòng phải từ hôm nay trở đi")
        LocalDate checkInDate,

        @Schema(description = "Ngày trả phòng, type: LocalDate, format: yyyy-MM-dd", example = "2026-10-15")
        @NotNull(message = "Ngày trả phòng không được để trống")
        @Future(message = "Ngày trả phòng phải lớn hơn hôm nay")
        LocalDate checkOutDate,

        @Schema(description = "Danh sách ID phòng cần đặt - rooms", example = "[1, 2]")
        @NotEmpty(message = "Phải chọn ít nhất 1 phòng để đặt")
        List<Long> roomIds,

        @Schema(description = "Số lượng khách", example = "2")
        @NotNull(message = "Vui lòng nhập số lượng khách")
        @Min(value = 1, message = "Số lượng khách ít nhất phải là 1")
        Integer totalGuests,

        @Schema(description = "Hình thức thanh toán (PAY_IN_FULL or PAY_AT_CHECKIN)", example = "PAY_IN_FULL")
        @NotNull(message = "Vui lòng chọn hình thức thanh toán")
        BookingPaymentOption paymentOption,

//        String promotionCode,

        @Schema(description = "Ghi chú thêm cho đặt phòng", example = "Tôi sẽ đến muộn, dự kiến lúc 8 giờ tối")
        String note
) {
}
