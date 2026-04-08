package com.stayhub.backend.Module.Property.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record PropertyCreateRequest(
        // ==========================================
        // 1. TỔNG QUAN & LOẠI HÌNH
        // ==========================================
        @Schema(description = "ID của hình thức cho thuê (Ví dụ: 1 = Nguyên căn, 2 = Phòng riêng)", example = "1")
        @NotNull(message = "Vui lòng chọn hình thức cho thuê (Rental Type)")
        Long rentalTypeId,

        @Schema(description = "ID của loại hình chỗ ở (Ví dụ: 1 = Homestay, 2 = Villa, 3 = Khách sạn)", example = "2")
        @NotNull(message = "Vui lòng chọn danh mục nhà (Category)")
        Long categoryId,

        @Schema(description = "Danh sách ID các tiện ích có trong nhà (Ví dụ: 1=Wifi, 2=Hồ bơi, 3=TV)", example = "[1, 2, 5, 8, 12]")
        @NotEmpty(message = "Vui lòng chọn ít nhất 1 tiện ích")
        List<Long> amenityIds,

        // ==========================================
        // 2. VỊ TRÍ ĐỊA LÝ
        // ==========================================
        @Schema(description = "Tỉnh / Thành phố", example = "Bà Rịa - Vũng Tàu")
        @NotBlank(message = "Tỉnh/Thành phố không được để trống")
        @Size(max = 100, message = "Tên Tỉnh/Thành phố quá dài")
        String province,

        @Schema(description = "Quận / Huyện", example = "Thành phố Vũng Tàu")
        @NotBlank(message = "Quận/Huyện không được để trống")
        @Size(max = 100, message = "Tên Quận/Huyện quá dài")
        String district,

        @Schema(description = "Phường / Xã", example = "Phường 1")
        @NotBlank(message = "Phường/Xã không được để trống")
        @Size(max = 100, message = "Tên Phường/Xã quá dài")
        String ward,

        @Schema(description = "Địa chỉ chi tiết (Ví dụ: Số nhà, tên đường)", example = "123 Đường ABC, Phường 1")
        @NotBlank(message = "Địa chỉ chi tiết không được để trống")
        @Size(max = 255, message = "Địa chỉ chi tiết không được vượt quá 255 ký tự")
        String addressDetail,

        @Schema(description = "Vĩ độ của chỗ ở (Giá trị từ -90 đến 90)", example = "10.762622")
        @NotNull(message = "Vĩ độ không được trống")
        @Min(value = -90, message = "Vĩ độ phải lớn hơn hoặc bằng -90")
        @Max(value = 90, message = "Vĩ độ phải nhỏ hơn hoặc bằng 90")
        Double latitude,

        @Schema(description = "Kinh độ của chỗ ở (Giá trị từ -180 đến 180)", example = "106.660172")
        @NotNull(message = "Kinh độ không được trống")
        @Min(value = -180, message = "Kinh độ phải lớn hơn hoặc bằng -180")
        @Max(value = 180, message = "Kinh độ phải nhỏ hơn hoặc bằng 180")
        Double longitude,

        // ==========================================
        // 4. THÔNG TIN BÀI ĐĂNG
        // ==========================================
        @Schema(description = "Tên chỗ ở (Từ 10 đến 255 ký tự)", example = "Biệt thự sang trọng tại Vũng Tàu")
        @NotBlank(message = "Tên chỗ ở không được để trống")
        @Size(min = 10, max = 255, message = "Tên chỗ ở phải từ 10 đến 255 ký tự")
        String name,

        @Schema(description = "Mô tả chi tiết về chỗ ở (Tối đa 5000 ký tự)", example = "Chỗ ở của chúng tôi có 3 phòng ngủ, hồ bơi riêng, và nằm gần biển...")
        @NotBlank(message = "Mô tả chỗ ở không được để trống")
        @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
        String description,

        // ==========================================
        // 5. ĐỊNH GIÁ & THANH TOÁN
        // ==========================================
        @Schema(description = "Phụ phi cuối tuần (Từ 0 - 100%)", example = "15")
        @Min(value = 0, message = "Phụ phí cuối tuần không được âm")
        @Max(value = 100, message = "Phụ phí cuối tuần tối đa là 100%")
        Integer weekendSurchargePercentage,

        @Schema(description = "Phí dọn dẹp (Từ 0 trở lên)", example = "50000")
        @DecimalMin(value = "0.0", message = "Phí dọn dẹp không được âm")
        BigDecimal cleaningFee,

        @Schema(description = "Số lượng phòng của chỗ ở (Từ 1 trở lên) - Chỉ nhập khi chọn hình thức thuê Toàn bộ chỗ ở", example = "")
        Integer roomCount,

        @Schema(description = "Danh sách URL hình ảnh của chỗ ở (Tối thiểu 5 hình ảnh để đảm bảo chất lượng tin đăng)", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\", \"https://example.com/image3.jpg\", \"https://example.com/image4.jpg\", \"https://example.com/image5.jpg\"]")
        @NotNull(message = "Danh sách ảnh không được để trống")
        @Size(min = 5, message = "Vui lòng tải lên tối thiểu 5 hình ảnh để đảm bảo chất lượng tin đăng")
        List<String> imageUrls,

        @Schema(description = "Tối thiểu 1 phòng - Thuê theo phòng, Duy nhất 1 phòng (đại diện cho chỗ ở) - Toàn bộ chỗ ở")
        @NotEmpty(message = "Chỗ ở phải có ít nhất 1 phòng")
        @Valid List<RoomCreateRequest> rooms
)
{ }
