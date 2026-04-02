package com.stayhub.backend.Module.Booking.DTO.Request;

import com.stayhub.backend.Common.Util.BookingPaymentOption;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record BookingCreateRequest(
        @NotNull(message = "ID căn nhà không được để trống")
        Long propertyId,

        @NotNull(message = "Ngày nhận phòng không được để trống")
        @FutureOrPresent(message = "Ngày nhận phòng phải từ hôm nay trở đi")
        LocalDate checkInDate,

        @NotNull(message = "Ngày trả phòng không được để trống")
        @Future(message = "Ngày trả phòng phải lớn hơn hôm nay")
        LocalDate checkOutDate,

        // 👉 FE CHỈ CẦN TRUYỀN MẢNG ID PHÒNG LÀ ĐỦ
        @NotEmpty(message = "Phải chọn ít nhất 1 phòng để đặt")
        List<Long> roomIds,

        @NotNull(message = "Vui lòng nhập số lượng khách")
        @Min(value = 1, message = "Số lượng khách ít nhất phải là 1")
        Integer totalGuests,

        @NotNull(message = "Vui lòng chọn hình thức thanh toán")
        BookingPaymentOption paymentOption,

//        String promotionCode,
        String note
) {
}
