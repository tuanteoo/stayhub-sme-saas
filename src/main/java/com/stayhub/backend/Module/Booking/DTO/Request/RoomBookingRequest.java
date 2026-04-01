package com.stayhub.backend.Module.Booking.DTO.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RoomBookingRequest(
        @NotNull(message = "ID phòng không được để trống")
        Long roomId,

        @Min(value = 1, message = "Số lượng khách mỗi phòng ít nhất phải là 1")
        Integer numGuests
) {
}
