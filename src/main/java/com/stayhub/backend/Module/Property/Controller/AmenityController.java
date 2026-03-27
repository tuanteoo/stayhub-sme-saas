package com.stayhub.backend.Module.Property.Controller;

import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Property.DTO.Request.AmenityRequest;
import com.stayhub.backend.Module.Property.DTO.Response.AmenityResponse;
import com.stayhub.backend.Module.Property.Service.AmenityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AmenityController {
    private final AmenityService amenityService;

    @PostMapping("/admin/amenities")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseData<AmenityResponse> createAmenity(@Valid @RequestBody AmenityRequest request) {
        AmenityResponse response = amenityService.createAmenity(request);
        return new ResponseData<>(201, "Tạo tiện ích thành công", response);
    }

    @Operation(summary = "Lấy danh sách tiện tích")
    @GetMapping("/public/amenities")
    public ResponseData<List<AmenityResponse>> getAllAmenity() {
        List<AmenityResponse> response = amenityService.getAllAmenities();
        return new ResponseData<>(200, "Lấy danh sách tiện ích thành công", response);
    }
}
