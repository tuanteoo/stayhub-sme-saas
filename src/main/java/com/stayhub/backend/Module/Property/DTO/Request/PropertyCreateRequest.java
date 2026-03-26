package com.stayhub.backend.Module.Property.DTO.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record PropertyCreateRequest(
        // ==========================================
        // 1. TỔNG QUAN & LOẠI HÌNH
        // ==========================================
        @NotNull(message = "Vui lòng chọn danh mục nhà (Category)")
        Long categoryId,

        @NotNull(message = "Vui lòng chọn hình thức cho thuê (Rental Type)")
        Long rentalTypeId,

        // ==========================================
        // 2. VỊ TRÍ ĐỊA LÝ
        // ==========================================
        @NotBlank(message = "Tỉnh/Thành phố không được để trống")
        String province,

        @NotBlank(message = "Quận/Huyện không được để trống")
        String district,

        @NotBlank(message = "Phường/Xã không được để trống")
        String ward,

        @NotBlank(message = "Địa chỉ chi tiết (Số nhà, Tên đường) không được để trống")
        String addressDetail,

        // ==========================================
        // 4. THÔNG TIN BÀI ĐĂNG
        // ==========================================
        @NotBlank(message = "Tên chỗ ở không được để trống")
        @Size(min = 10, max = 255, message = "Tên chỗ ở phải từ 10 đến 255 ký tự")
        String name,

        @NotBlank(message = "Mô tả chỗ ở không được để trống")
        String description,

        // ==========================================
        // 5. ĐỊNH GIÁ & THANH TOÁN
        // ==========================================
        @Min(value = 0, message = "Phụ phí cuối tuần không được âm")
        @Max(value = 100, message = "Phụ phí cuối tuần tối đa là 100%")
        Integer weekendSurchargePercentage,

        @DecimalMin(value = "0.0", message = "Phí dọn dẹp không được âm")
        BigDecimal cleaningFee,

        @NotNull(message = "Vui lòng cấu hình Cho phép thanh toán khi nhận phòng")
        Boolean isPayAtCheckinAllowed,

        @Min(value = 0, message = "Mức cọc không được nhỏ hơn 0%")
        @Max(value = 100, message = "Mức cọc không được lớn hơn 100%")
        Integer depositPercentage,

        // ==========================================
        // 6. CHÍNH SÁCH, TIỆN ÍCH & HÌNH ẢNH
        // ==========================================
        @NotNull(message = "Vui lòng chọn Chính sách hủy phòng")
        Long cancellationPolicyId,

        @NotEmpty(message = "Vui lòng chọn ít nhất 1 tiện ích")
        List<Long> amenityIds,

        @NotNull(message = "Danh sách ảnh không được để trống")
        @Size(min = 5, message = "Vui lòng tải lên tối thiểu 5 hình ảnh (Mặt tiền, phòng ngủ, phòng tắm...) để đảm bảo chất lượng tin đăng")
        List<String> imageUrls,

        @NotEmpty(message = "Chỗ ở phải có ít nhất 1 phòng")
        @Valid List<RoomCreateRequest> rooms
) {
}
