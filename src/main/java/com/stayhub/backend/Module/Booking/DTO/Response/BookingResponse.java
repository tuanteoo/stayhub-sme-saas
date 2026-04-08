package com.stayhub.backend.Module.Booking.DTO.Response;

import com.stayhub.backend.Common.Util.BookingPaymentOption;
import com.stayhub.backend.Common.Util.BookingStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record BookingResponse(
        Long id,
        String bookingCode,

        // Thông tin nhà
        Long propertyId,
        String propertyName,
        String propertyAddress,
        String propertyThumbnailUrl,

        // Thời gian
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Integer totalNights,

        BigDecimal totalPrice,      // Tổng tiền phòng (Đã nhân số đêm)
        BigDecimal cleaningFee,     // Phí dọn dẹp
        BigDecimal serviceFee,      // Phí dịch vụ (Của nền tảng)
        BigDecimal discountAmount,  // Tiền được giảm giá
        BigDecimal finalAmount,     // Tổng thanh toán cuối cùng (Tổng phòng + Phí - Giảm giá)

        // Trạng thái thanh toán
        BigDecimal depositAmount,   // Tiền phải cọc
        BigDecimal remainingAmount, // Tiền còn lại phải trả
        BookingPaymentOption paymentOption,
        BookingStatus status,
        Boolean isFullyPaid,

        String note,
        LocalDateTime createdAt,

        List<BookingRoomResponse> bookedRooms
) {
}
