package com.stayhub.backend.Common.Util;

public enum PaymentStatus {
    PENDING,    // Đang chờ khách thanh toán
    COMPLETED,  // Đã thanh toán thành công
    FAILED,     // Thanh toán thất bại (Lỗi thẻ, rớt mạng...)
    CANCELLED,  // Giao dịch bị hủy bỏ
    REFUNDED
}
