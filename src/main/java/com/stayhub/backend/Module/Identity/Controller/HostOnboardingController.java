package com.stayhub.backend.Module.Identity.Controller;

import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Identity.DTO.Request.HostVerificationRequest;
import com.stayhub.backend.Module.Identity.Service.HostOnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class HostOnboardingController {
    private final HostOnboardingService hostOnboardingService;

    @PostMapping("/host-applications")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseData<String> submitApplication(
            Principal principal,
            @Valid @RequestBody HostVerificationRequest request) {
        hostOnboardingService.submitHostApplication(principal.getName(), request);

        return new ResponseData<>(201,
                "Gửi hồ sơ đăng ký thành công! Vui lòng chờ Ban quản trị StayHub phê duyệt.");
    }
}
