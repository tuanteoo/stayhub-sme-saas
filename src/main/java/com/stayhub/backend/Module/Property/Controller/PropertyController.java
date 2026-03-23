package com.stayhub.backend.Module.Property.Controller;

import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import com.stayhub.backend.Module.Property.Service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("properties")
@RequiredArgsConstructor
public class PropertyController {
    private final PropertyService propertyService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseData<String> createProperty(
            Principal principal,
            @Valid @RequestBody PropertyCreateRequest request) {

        propertyService.createProperty(principal.getName(), request);

        return new ResponseData<>(
                201,
                "Tạo tin đăng thành công! Tin đăng của bạn đang ở trạng thái Chờ duyệt."
        );
    }
}
