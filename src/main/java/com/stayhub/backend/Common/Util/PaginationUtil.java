package com.stayhub.backend.Common.Util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {
    /**
     * Hàm cấu hình phân trang hỗ trợ Sort linh hoạt cho toàn hệ thống
     *
     * @param page    Trang hiện tại (Frontend gửi 1 là trang đầu tiên)
     * @param size    Số lượng phần tử trên 1 trang
     * @param sortBy  Tên cột cần sắp xếp (ví dụ: "createdAt", "pricePerNight")
     * @param sortDir Hướng sắp xếp ("asc" hoặc "desc")
     * @return Đối tượng Pageable chuẩn của Spring Data
     */

    public static Pageable getPageable(int page, int size, String sortBy, String sortDir) {
        int pageNumber = (page > 0) ? page - 1 : 0;

        // Cấu hình hướng sắp xếp
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return PageRequest.of(pageNumber, size, sort);
    }

    public static Pageable getPageable(int page, int size) {
        return getPageable(page, size, "createdAt", "desc");
    }
}
