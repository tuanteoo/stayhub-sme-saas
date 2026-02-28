package com.stayhub.backend.Common.Util;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống chưa được định nghĩa", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Sai key xác thực", HttpStatus.BAD_REQUEST),

    USER_EXISTED(1002, "Người dùng đã tồn tại", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1003, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(1004, "Tên đăng nhập phải có ít nhất 3 ký tự", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1005, "Mật khẩu phải có ít nhất 8 ký tự", HttpStatus.BAD_REQUEST),
    INVALID_DATA(1006, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST);


    private final int code;
    private final String message;
    private final HttpStatus statusCode;

    ErrorCode(int code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
