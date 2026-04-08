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
    INVALID_DATA(1006, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    // --- BỔ SUNG MỚI CHO AUTH & IDENTITY ---
    EMAIL_EXISTED(1007, "Email này đã được đăng ký", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(1008, "Lỗi cấu hình: Không tìm thấy vai trò hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_TOKEN(1009, "Mã xác thực không hợp lệ hoặc không tồn tại", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1010, "Mã xác thực đã hết hạn, vui lòng yêu cầu gửi lại", HttpStatus.BAD_REQUEST),
    USER_ALREADY_VERIFIED(1011, "Tài khoản này đã được xác thực trước đó", HttpStatus.BAD_REQUEST),
    HOST_PROFILE_NOT_FOUND(1012, "Không tìm thấy hồ sơ Chủ nhà của User này", HttpStatus.NOT_FOUND),
    HOST_NOT_APPROVED(1013, "Chỉ Chủ nhà đã xác thực mới có quyền đăng tin", HttpStatus.FORBIDDEN),
    CATEGORY_NOT_FOUND(1014, "Không tìm thấy danh mục nhà", HttpStatus.NOT_FOUND),
    RENTAL_TYPE_NOT_FOUND(1015, "Không tìm thấy loại hình cho thuê", HttpStatus.NOT_FOUND),
    POLICY_NOT_FOUND(1016, "Không tìm thấy chính sách hủy phòng", HttpStatus.NOT_FOUND),
    PASSWORD_EMAIL_WRONG(1017, "Email hoặc mật khẩu không chính xác", HttpStatus.NOT_FOUND),
    EMAIL_SEND_FAILED(1018, "Gửi email thất bại, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus statusCode;

    ErrorCode(int code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
