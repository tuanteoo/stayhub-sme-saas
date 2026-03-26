package com.stayhub.backend.Module.Identity.Controller;

import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Identity.DTO.Request.*;
import com.stayhub.backend.Module.Identity.DTO.Response.LoginResponse;
import com.stayhub.backend.Module.Identity.DTO.Response.TokenRefreshResponse;
import com.stayhub.backend.Module.Identity.Service.AuthService;
import com.stayhub.backend.Module.Identity.Service.HostOnboardingService;
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

    @PostMapping("/register-guest")
    public ResponseData<String> registerGuest(@Valid @RequestBody RegisterGuestRequest registerRequest) {
        String message = authService.registerGuest(registerRequest);
        return new ResponseData<>(HttpStatus.CREATED.value(), message);
    }

    @GetMapping("/verify-email")
    public ResponseData<String> verifyAccount(@RequestParam("token") String token) {
        String message = authService.verifyEmailToken(token);
        return new ResponseData<>(HttpStatus.OK.value(), message);
    }

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

    @PostMapping("/logout")
    public ResponseData<String> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return new ResponseData<>(HttpStatus.OK.value(), "Đăng xuất thành công");
    }

    @PostMapping("/host-applications")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseData<String> submitApplication(
            Principal principal,
            @Valid @RequestBody HostRegistrationWithPropertyRequest request) {
        hostOnboardingService.submitHostApplication(principal.getName(), request);

        return new ResponseData<>(201,
                "Gửi hồ sơ đăng ký thành công! Vui lòng chờ Ban quản trị StayHub phê duyệt.");
    }

    @PutMapping("/{userId}/approval-host")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ResponseData<String>> reviewApplication(
            @PathVariable Long userId,
            @Valid @RequestBody HostApprovalRequest request) {

        hostOnboardingService.reviewHostApplication(userId, request);

        String message = switch (request.status()) {
            case APPROVED -> "Đã duyệt hồ sơ và cấp quyền Chủ nhà thành công!";
            case REJECTED -> "Đã từ chối hồ sơ Chủ nhà!";
            case REQUEST_CHANGES -> "Đã gửi yêu cầu bổ sung/chỉnh sửa hồ sơ cho User!";
            default -> "Đã cập nhật trạng thái hồ sơ!";
        };

        return ResponseEntity.ok(new ResponseData<>(200, message));
    }
}
