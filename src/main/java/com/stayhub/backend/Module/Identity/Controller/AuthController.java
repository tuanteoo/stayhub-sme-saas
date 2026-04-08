package com.stayhub.backend.Module.Identity.Controller;

import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Identity.DTO.Request.*;
import com.stayhub.backend.Module.Identity.DTO.Response.LoginResponse;
import com.stayhub.backend.Module.Identity.DTO.Response.TokenRefreshResponse;
import com.stayhub.backend.Module.Identity.Service.AuthService;
import com.stayhub.backend.Module.Identity.Service.HostOnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final HostOnboardingService hostOnboardingService;

    @Operation(summary = "All - Tạo tài khoản và gửi email xác thực",
    description = """
            Phương thức: POST
            
            Đường dẫn: /api/v1/auth/register-guest
            
            Đối tượng yêu cầu: RegisterGuestRequest
            
            Quy tắc xác thực Mật khẩu: Mật khẩu bắt buộc tuân theo biểu thức ^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$. Cụ thể, mật khẩu phải dài tối thiểu 8 ký tự, bao gồm ít nhất một chữ cái viết hoa, một chữ cái viết thường, một chữ số và một ký tự đặc biệt.
            
            Chi tiết phản hồi: Trả về chuỗi thông báo thành công và yêu cầu người dùng kiểm tra thư điện tử để xác thực tài khoản.
            """)
    @PostMapping("/register-guest")
    public ResponseData<String> registerGuest(@Valid @RequestBody RegisterGuestRequest registerRequest) {
        String message = authService.registerGuest(registerRequest);
        return new ResponseData<>(HttpStatus.CREATED.value(), message);
    }

    @Operation(summary = "All - Xác thực email để kích hoạt tài khoản")
    @GetMapping("/verify-email")
    public ResponseData<String> verifyAccount(@RequestParam("token") String token) {
        String message = authService.verifyEmailToken(token);
        return new ResponseData<>(HttpStatus.OK.value(), message);
    }

    @Operation(summary = "All - Đăng nhập")
    @PostMapping("/login")
    public ResponseData<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return new ResponseData<>(HttpStatus.OK.value(), "Đăng nhập thành công", response);
    }

    @PostMapping("/refresh-token")
    public ResponseData<TokenRefreshResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return new ResponseData<>(200, "Làm mới token thành công", response);
    }

    @Operation(summary = "Người dùng đã xác thực - Đăng xuất khỏi hệ thống")
    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_HOST') or hasAuthority('ROLE_ADMIN')")
    public ResponseData<String> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return new ResponseData<>(HttpStatus.OK.value(), "Đăng xuất thành công");
    }

    @PostMapping("/host-applications")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "USER - Đăng ký chủ nhà",
    description = """
            Phương thức: POST
            
            Đường dẫn: /api/v1/auth/host-applications
            
            Đối tượng yêu cầu: HostRegistrationWithPropertyRequest
            
            Quy tắc xác thực Số điện thoại: Thuộc tính businessPhone tuân theo biểu thức ^(0|84|\\+84)(3[2-9]|5[2689]|7[06-9]|8[1-9]|9[0-9])([0-9]{8})$. Cụ thể, số điện thoại phải bắt đầu bằng 0, 84 hoặc +84, tiếp theo là mã mạng hợp lệ của Việt Nam và kết thúc bằng 8 chữ số.
            
            Quy tắc xác thực Căn cước công dân: Thuộc tính identityCardNumber tuân theo biểu thức ^[0-9]{12}$, yêu cầu chính xác 12 chữ số liên tiếp.
            
            Chi tiết phản hồi: Trả về chuỗi thông báo gửi hồ sơ thành công và đang chờ ban quản trị phê duyệt.
            """)
    public ResponseData<String> submitApplication(
            Principal principal,
            @Valid @RequestBody HostRegistrationWithPropertyRequest request) {
        String hostCode = hostOnboardingService.submitHostApplication(principal.getName(), request);

        return new ResponseData<>(201,
                "Gửi hồ sơ đăng ký thành công! Vui lòng chờ Ban quản trị StayHub phê duyệt.", hostCode);
    }

    @PutMapping("/{id}/approval-host")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "ADMIN - Duyệt hồ sơ đăng ký chủ nhà",
    description = """
            Phương thức: PUT
            
            Đường dẫn: /api/v1/auth/{id}/approval-host
            
            Tham số đường dẫn: id (Định danh người dùng)
            
            Đối tượng yêu cầu: HostApprovalRequest
            
            Chi tiết phản hồi: Trả về chuỗi thông báo trạng thái phê duyệt tương ứng với kết quả quyết định.
            """)
    public ResponseEntity<ResponseData<String>> reviewApplication(
            @PathVariable String hostCode,
            @Valid @RequestBody HostApprovalRequest request) {
        hostOnboardingService.reviewHostApplication(hostCode, request);

        String message = switch (request.status()) {
            case APPROVED -> "Đã duyệt hồ sơ và cấp quyền Chủ nhà thành công!";
            case REJECTED -> "Đã từ chối hồ sơ Chủ nhà!";
            case REQUEST_CHANGES -> "Đã gửi yêu cầu bổ sung/chỉnh sửa hồ sơ cho User!";
            default -> "Đã cập nhật trạng thái hồ sơ!";
        };

        return ResponseEntity.ok(new ResponseData<>(200, message));
    }
}
