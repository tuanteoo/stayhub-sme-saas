package com.stayhub.backend.Module.Property.Controller;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import com.stayhub.backend.Module.Property.DTO.Response.PropertyCardResponse;
import com.stayhub.backend.Module.Property.Service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<ResponseData<PageResponse<PropertyCardResponse>>> getProperties(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Integer guestCount) {

        PageResponse<PropertyCardResponse> properties = propertyService.getPropertiesForGuest(page, size, sortBy, sortDir,destination,guestCount);

        return ResponseEntity.ok(new ResponseData<>(200, "Lấy danh sách thành công", properties));
    }
}
