package com.stayhub.backend.Module.Identity.Controller;

import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Identity.DTO.Request.LoginRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.LogoutRequest;
import com.stayhub.backend.Module.Identity.DTO.Request.RegisterGuestRequest;
import com.stayhub.backend.Module.Identity.DTO.Response.LoginResponse;
import com.stayhub.backend.Module.Identity.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

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

    @PostMapping("/logout")
    public ResponseData<String> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return new ResponseData<>(HttpStatus.OK.value(), "Đăng xuất thành công");
    }
}
