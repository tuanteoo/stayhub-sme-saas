package com.stayhub.backend.Module.Property.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record RoomCreateRequest(
        @Schema(description = "Tên phòng (Ví dụ: Phòng Deluxe, Phòng Superior)", example = "Phòng Deluxe")
        @NotBlank(message = "Tên phòng không được để trống")
        @Size(max = 255, message = "Tên phòng không được vượt quá 255 ký tự")
        String name,

        @Schema(description = "Mô tả chi tiết về phòng (Ví dụ: Phòng rộng rãi với giường king-size, ban công hướng biển)", example = "Phòng rộng rãi với giường king-size, ban công hướng biển")
        @Size(max = 2000, message = "Mô tả phòng không được vượt quá 2000 ký tự")
        String description,

        @Schema(description = "Giá phòng mỗi đêm (Ví dụ: 1500000.00)", example = "1500000")
        @NotNull(message = "Giá phòng không được để trống")
        @DecimalMin(value = "1000.0", message = "Giá phòng mỗi đêm tối thiểu là 1,000")
        BigDecimal pricePerNight,

        @Schema(description = "Số lượng khách tối đa có thể ở trong phòng này (Ví dụ: 2)", example = "2")
        @NotNull(message = "Số lượng khách tối đa không được để trống")
        @Min(value = 1, message = "Số lượng khách tối đa phải từ 1 trở lên")
        Integer maxGuests,

        @Schema(description = "Số lượng giường trong phòng (Ví dụ: 1)", example = "1")
        @NotNull(message = "Số lượng giường không được để trống")
        @Min(value = 1, message = "Số lượng giường phải từ 1 trở lên")
        Integer numBeds,

        @Schema(description = "Số lượng phòng tắm trong phòng (Ví dụ: 1)", example = "1")
        @NotNull(message = "Số lượng phòng tắm không được để trống")
        @Min(value = 0, message = "Số lượng phòng tắm không hợp lệ")
        Integer numBathrooms,

        @Schema(description = "Danh sách ID các tiện ích có trong phòng này (Ví dụ: 1=Wifi, 2=Hồ bơi, 3=TV)", example = "[1, 2, 5, 8, 12]")
        List<Long> amenityIds,

        @Schema(description = "Danh sách URL hình ảnh của phòng này", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
        @NotEmpty(message = "Vui lòng tải lên ít nhất 1 hình ảnh cho phòng này")
        List<String> imageUrls
) {
}
