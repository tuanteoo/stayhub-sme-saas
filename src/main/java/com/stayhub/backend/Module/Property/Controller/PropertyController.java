package com.stayhub.backend.Module.Property.Controller;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Identity.Security.CustomUserDetails;
import com.stayhub.backend.Module.Property.DTO.Response.HostPropertyResponse;
import com.stayhub.backend.Module.Property.DTO.Response.PropertyDetailResponse;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import com.stayhub.backend.Module.Property.DTO.Response.PropertyCardResponse;
import com.stayhub.backend.Module.Property.DTO.Response.RoomPriceResponse;
import com.stayhub.backend.Module.Property.Service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
@Tag(name = "Property", description = "API Bài đăng về tài sản cho thuê")
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

    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @Operation(summary = "ROLE_HOST - Lấy danh sách bài đăng")
    @GetMapping("/host")
    public ResponseEntity<ResponseData<PageResponse<HostPropertyResponse>>> getPropertiesByHost(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponse<HostPropertyResponse> response = propertyService.getPropertiesByHost(
                customUserDetails.getUser().getId(), page, size, sortBy, sortDir);

        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Lấy danh sách bài đăng của Host thành công", response));
    }

    @Operation(summary = "Tất cả người dùng - API này tìm kiếm và lấy danh sách các tài sản cho thuê dựa trên các tiêu chí lọc như điểm đến, số lượng khách, ngày nhận phòng và ngày trả phòng.")
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

    @Operation(summary = "Tất cả người dùng - API này xem chi tiết một tài sản cho thuê dựa trên slug duy nhất của nó.")
    @GetMapping("/{slug}")
    public ResponseEntity<ResponseData<PropertyDetailResponse>> getPropertyDetail(
            @PathVariable String slug,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {

        PropertyDetailResponse propertyDetail = propertyService.getPropertyBySlug(slug, checkInDate, checkOutDate);

        return ResponseEntity.ok(new ResponseData<>(200, "Lấy thông tin chi tiết thành công", propertyDetail));
    }

    @Operation(summary = "Tất cả người dùng - Tính toán giá tiền cho một tài sản cho thuê dựa trên slug duy nhất của nó và khoảng thời gian lưu trú.")
    @GetMapping("/{slug}/calculate-price")
    public ResponseEntity<ResponseData<List<RoomPriceResponse>>> calculatePriceBySlug(
            @PathVariable String slug,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {

        List<RoomPriceResponse> response = propertyService.calculatePriceForProperty(slug, checkInDate, checkOutDate);
        return ResponseEntity.ok(new ResponseData<>(200, "Tính giá thành công", response));
    }
}
