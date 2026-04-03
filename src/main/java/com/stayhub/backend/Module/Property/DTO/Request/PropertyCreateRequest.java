package com.stayhub.backend.Module.Property.DTO.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record PropertyCreateRequest(
        // ==========================================
        // 1. TỔNG QUAN & LOẠI HÌNH
        // ==========================================
        @NotNull(message = "Vui lòng chọn hình thức cho thuê (Rental Type)")
        Long rentalTypeId,

        @NotNull(message = "Vui lòng chọn danh mục nhà (Category)")
        Long categoryId,

        @NotEmpty(message = "Vui lòng chọn ít nhất 1 tiện ích")
        List<Long> amenityIds,

        // ==========================================
        // 2. VỊ TRÍ ĐỊA LÝ
        // ==========================================
        @NotBlank(message = "Tỉnh/Thành phố không được để trống")
        @Size(max = 100, message = "Tên Tỉnh/Thành phố quá dài")
        String province,

        @NotBlank(message = "Quận/Huyện không được để trống")
        @Size(max = 100, message = "Tên Quận/Huyện quá dài")
        String district,

        @NotBlank(message = "Phường/Xã không được để trống")
        @Size(max = 100, message = "Tên Phường/Xã quá dài")
        String ward,

        @NotBlank(message = "Địa chỉ chi tiết không được để trống")
        @Size(max = 255, message = "Địa chỉ chi tiết không được vượt quá 255 ký tự")
        String addressDetail,

        @NotNull(message = "Vĩ độ không được trống")
        @Min(value = -90, message = "Vĩ độ phải lớn hơn hoặc bằng -90")
        @Max(value = 90, message = "Vĩ độ phải nhỏ hơn hoặc bằng 90")
        Double latitude,

        @NotNull(message = "Kinh độ không được trống")
        @Min(value = -180, message = "Kinh độ phải lớn hơn hoặc bằng -180")
        @Max(value = 180, message = "Kinh độ phải nhỏ hơn hoặc bằng 180")
        Double longitude,

        // ==========================================
        // 4. THÔNG TIN BÀI ĐĂNG
        // ==========================================
        @NotBlank(message = "Tên chỗ ở không được để trống")
        @Size(min = 10, max = 255, message = "Tên chỗ ở phải từ 10 đến 255 ký tự")
        String name,

        @NotBlank(message = "Mô tả chỗ ở không được để trống")
        @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
        String description,

        // ==========================================
        // 5. ĐỊNH GIÁ & THANH TOÁN
        // ==========================================
        @Min(value = 0, message = "Phụ phí cuối tuần không được âm")
        @Max(value = 100, message = "Phụ phí cuối tuần tối đa là 100%")
        Integer weekendSurchargePercentage,

        @DecimalMin(value = "0.0", message = "Phí dọn dẹp không được âm")
        BigDecimal cleaningFee,

        Integer roomCount,

        // ==========================================
        // 6. CHÍNH SÁCH, TIỆN ÍCH & HÌNH ẢNH
        // ==========================================

        @NotNull(message = "Danh sách ảnh không được để trống")
        @Size(min = 5, message = "Vui lòng tải lên tối thiểu 5 hình ảnh để đảm bảo chất lượng tin đăng")
        List<String> imageUrls,

        @NotEmpty(message = "Chỗ ở phải có ít nhất 1 phòng")
        @Valid List<RoomCreateRequest> rooms
) {
}
