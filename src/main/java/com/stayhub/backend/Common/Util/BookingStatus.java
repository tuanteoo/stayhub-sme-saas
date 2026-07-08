package com.stayhub.backend.Common.Util;

public enum BookingStatus {
    PENDING,            // Vừa tạo đơn, chờ xử lý
    AWAITING_PAYMENT,   // Đang chờ khách thanh toán
    PARTIALLY_PAID,     // Đã cọc một phần
    CONFIRMED,          // Đã xác nhận (đã thanh toán đủ hoặc thanh toán sau)
    CHECKED_IN,         // Khách đã nhận phòng
    CHECKED_OUT,        // Khách đã trả phòng
    DISPUTED,           // Có tranh chấp/khiếu nại
    COMPLETED,          // Hoàn tất (đã review, không có vấn đề gì)
    CANCELLED,          // Khách hoặc Host hủy
    REJECTED,           // Hệ thống từ chối
    EXPIRED             // Hết hạn thanh toán
}
