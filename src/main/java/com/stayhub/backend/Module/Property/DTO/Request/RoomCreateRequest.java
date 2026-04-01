package com.stayhub.backend.Module.Property.DTO.Request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record RoomCreateRequest(
        @NotBlank(message = "Tên phòng không được để trống")
        @Size(max = 255, message = "Tên phòng không được vượt quá 255 ký tự")
        String name,

        @Size(max = 2000, message = "Mô tả phòng không được vượt quá 2000 ký tự")
        String description,

        @NotNull(message = "Giá phòng không được để trống")
        @DecimalMin(value = "1000.0", message = "Giá phòng mỗi đêm tối thiểu là 1,000")
        BigDecimal pricePerNight,

        @NotNull(message = "Số lượng khách tối đa không được để trống")
        @Min(value = 1, message = "Số lượng khách tối đa phải từ 1 trở lên")
        Integer maxGuests,

        @NotNull(message = "Số lượng giường không được để trống")
        @Min(value = 1, message = "Số lượng giường phải từ 1 trở lên")
        Integer numBeds,

        @NotNull(message = "Số lượng phòng tắm không được để trống")
        @Min(value = 0, message = "Số lượng phòng tắm không hợp lệ")
        Integer numBathrooms,

        List<Long> amenityIds,

        @NotEmpty(message = "Vui lòng tải lên ít nhất 1 hình ảnh cho phòng này")
        List<String> imageUrls
) {
}
