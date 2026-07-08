package com.stayhub.backend.Common.Util;

public enum UserSubscriptionStatus {
    ACTIVE,         // Đang hoạt động
    EXPIRED,        // Đã hết hạn
    CANCELLED,      // Bị hủy (do host chủ động hủy)
    PAYMENT_FAILED,
    UPGRADED
}
