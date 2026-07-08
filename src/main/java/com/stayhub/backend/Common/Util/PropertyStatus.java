package com.stayhub.backend.Common.Util;

public enum PropertyStatus {
    DRAFT,           // Đang soạn thảo, chưa nộp duyệt
    PENDING_REVIEW,  // Đã nộp hồ sơ, đang chờ Quản trị viên duyệt
    ACTIVE,          // Đang hoạt động, sẵn sàng đón khách
    INACTIVE,        // Chủ nhà chủ động tạm ngưng nhận khách

    REJECTED,        // Quản trị viên từ chối cấp phép
    HIDDEN,          // Quản trị viên ẩn bài do vi phạm nhẹ
    BANNED           // Quản trị viên cấm vĩnh viễn
}

