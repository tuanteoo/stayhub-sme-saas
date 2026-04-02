package com.stayhub.backend.Module.Property.Controller;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Identity.Security.CustomUserDetails;
import com.stayhub.backend.Module.Property.DTO.Response.PropertyDetailResponse;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import com.stayhub.backend.Module.Property.DTO.Response.PropertyCardResponse;
import com.stayhub.backend.Module.Property.Service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
public class PropertyController {
    private final PropertyService propertyService;

    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @Operation(summary = "Chủ nhà ROLE_HOST - API này cho phép chủ nhà tạo mới một danh mục tài sản cho thuê.",
    description = """
            Phương thức: POST
            
            Đường dẫn: /api/v1/properties
            
            Đối tượng yêu cầu: PropertyCreateRequest
            
            Chi tiết phản hồi: Trả về chuỗi thông báo tạo tin đăng thành công và chờ duyệt.
            """)
    @PostMapping
    public ResponseData<String> createProperty(@AuthenticationPrincipal CustomUserDetails customUserDetails, @Valid @RequestBody PropertyCreateRequest request) {

        propertyService.createProperty(customUserDetails.getUser().getEmail(), request);

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
            @RequestParam(required = false) Integer guestCount,
            @RequestParam(required = false) LocalDate checkInDate,
            @RequestParam(required = false) LocalDate checkOutDate){

        PageResponse<PropertyCardResponse> properties = propertyService.getPropertiesForGuest(page, size, sortBy, sortDir,destination,guestCount,checkInDate,checkOutDate);

        return ResponseEntity.ok(new ResponseData<>(200, "Lấy danh sách thành công", properties));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ResponseData<PropertyDetailResponse>> getPropertyDetail(@PathVariable String slug) {

        PropertyDetailResponse propertyDetail = propertyService.getPropertyBySlug(slug);

        return ResponseEntity.ok(new ResponseData<>(200, "Lấy thông tin chi tiết thành công", propertyDetail));
    }
}
