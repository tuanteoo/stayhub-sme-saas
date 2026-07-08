package com.stayhub.backend.Common.Util;

public enum PaymentPurpose {
    BOOKING_PAYMENT,        // Thanh toán tiền đặt phòng (Bao gồm cả cọc hoặc toàn bộ)
    SUBSCRIPTION_PAYMENT,   // Chủ nhà thanh toán gói Subscription
    REFUND,                 // Hoàn tiền cho khách
    PAYOUT
}
