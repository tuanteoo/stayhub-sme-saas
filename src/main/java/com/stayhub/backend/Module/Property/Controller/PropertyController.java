package com.stayhub.backend.Module.Property.Controller;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Identity.Security.CustomUserDetails;
import com.stayhub.backend.Module.Property.DTO.Response.*;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import com.stayhub.backend.Module.Property.Service.CancellationPolicyService;
import com.stayhub.backend.Module.Property.Service.CategoryService;
import com.stayhub.backend.Module.Property.Service.PropertyService;
import com.stayhub.backend.Module.Property.Service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
@Tag(name = "Property", description = "API về Tài sản")
public class PropertyController {
    private final PropertyService propertyService;
    private final CategoryService categoryService;
    private final SubscriptionService subscriptionService;
    private final CancellationPolicyService cancellationPolicyService;

    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @Operation(summary = "HOST - Tạo bài đăng",
    description = """
            Phương thức: POST
            Đối tượng yêu cầu: PropertyCreateRequest
            """)
    @PostMapping
    public ResponseData<String> createProperty(@AuthenticationPrincipal CustomUserDetails customUserDetails, @Valid @RequestBody PropertyCreateRequest request) {

        propertyService.createProperty(customUserDetails.getUser().getId(), request, false);

        return new ResponseData<>(
                201,
                "Tạo tin đăng thành công! Tin đăng của bạn đang ở trạng thái Chờ duyệt."
        );
    }

    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @Operation(summary = "HOST - Lấy danh sách bài đăng")
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

    @Operation(summary = "GUEST - Lấy 8 bài đăng nổi bật theo category slug")
    @GetMapping("/category/{slug}/top")
    public ResponseEntity<ResponseData<List<PropertyCardResponse>>> getTopPropertiesByCategorySlug(
            @PathVariable("slug") String categorySlug) {

        List<PropertyCardResponse> response = propertyService.getTopPropertiesByCategorySlug(categorySlug);
        return ResponseEntity.ok(new ResponseData<>(200, "Lấy danh sách bài đăng theo category thành công", response));
    }

    @Operation(summary = "GUEST_USER - Tìm kiếm và lọc bài đăng dựa trên các tiêu chí như điểm đến, số lượng khách, ngày nhận phòng, ngày trả phòng và danh mục.")
    @GetMapping
    public ResponseEntity<ResponseData<PageResponse<PropertyCardResponse>>> getProperties(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Integer guestCount,
            @RequestParam(required = false) LocalDate checkInDate,
            @RequestParam(required = false) LocalDate checkOutDate,
            @RequestParam(required = false) String categorySlug) {

        PageResponse<PropertyCardResponse> properties = propertyService.getPropertiesForGuest(page, size, sortBy, sortDir,destination,guestCount,checkInDate,checkOutDate, categorySlug);

        return ResponseEntity.ok(new ResponseData<>(200, "Lấy danh sách thành công", properties));
    }

    @Operation(summary = "ADMIN - Lấy danh sách tất cả bài đăng(hồ sơ host đã được chấp thuận)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<ResponseData<PageResponse<AdminPropertyResponse>>> getPropertiesForAdmin(
            @Parameter(
                    name = "status",
                    description = "Trạng thái bài đăng để lọc (DRAFT, PENDING_REVIEW, PUBLISHED, HIDDEN, BANNED, REJECTED)"
            )
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponse<AdminPropertyResponse> response = propertyService.getPropertiesForAdmin(status, pageNo, pageSize, sortBy, sortDir);
        return ResponseEntity.ok(new ResponseData<>(200, "Lấy danh sách bài đăng thành công", response));
    }

    @Operation(summary = "GUEST_USER - Xem chi tiết bài đăng dựa trên slug")
    @GetMapping("/{slug}")
    public ResponseEntity<ResponseData<PropertyDetailResponse>> getPropertyDetail(
            @PathVariable String slug,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {

        PropertyDetailResponse propertyDetail = propertyService.getPropertyBySlug(slug, checkInDate, checkOutDate);

        return ResponseEntity.ok(new ResponseData<>(200, "Lấy thông tin chi tiết thành công", propertyDetail));
    }

    @Operation(summary = "GUEST_USER - Tính tiền cho phòng đã chọn dựa trên slug bài đăng, ngày nhận phòng và ngày trả phòng")
    @GetMapping("/{slug}/calculate-price")
    public ResponseEntity<ResponseData<List<RoomPriceResponse>>> calculatePriceBySlug(
            @PathVariable String slug,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) List<Long> roomIds) {

        List<RoomPriceResponse> response = propertyService.calculatePriceForProperty(slug, checkInDate, checkOutDate, roomIds);
        return ResponseEntity.ok(new ResponseData<>(200, "Tính giá thành công", response));
    }

    @Operation(summary = "All - Lấy danh sách tất cả danh mục")
    @GetMapping("/categories")
    public ResponseEntity<ResponseData<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(new ResponseData<>(200, "Lấy danh mục thành công", categoryService.getAllCategories()));
    }

    @Operation(summary = "HOST - Lấy thông tin gói cước của Host")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @GetMapping("/host/my-subscription")
    public ResponseEntity<ResponseData<MySubscriptionResponse>> getMySubscription(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        MySubscriptionResponse response = subscriptionService.getMySubscription(customUserDetails.getUser().getId());
        return ResponseEntity.ok(new ResponseData<>(200, "Thành công", response));
    }

    @Operation(summary = "HOST - Lấy danh sách gói cước hoạt động")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @GetMapping("/host/subscription-plans")
    public ResponseEntity<ResponseData<java.util.List<SubscriptionPlanResponse>>> getActiveSubscriptionPlans() {

        java.util.List<SubscriptionPlanResponse> response = subscriptionService.getActiveSubscriptionPlans();

        return ResponseEntity.ok(new ResponseData<>(200, "Lấy danh sách gói cước thành công", response));
    }

    @GetMapping("host/cancellation-policy")
    @Operation(summary = "HOST - Lấy danh sách chính sách hủy đang hoạt động")
    public ResponseEntity<ResponseData<List<CancellationPolicyResponse>>> getActivePolicies() {

        List<CancellationPolicyResponse> data = cancellationPolicyService.getActivePolicies();

        ResponseData<List<CancellationPolicyResponse>> response = new ResponseData<>(
                HttpStatus.OK.value(),
                "Ly danh sch chnh sch hy thnh cng",
                data
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "ADMIN - Thẩm định bài đăng của host")
    @PutMapping("/admin/{propertyId}/review")
    public ResponseEntity<ResponseData<String>> reviewProperty(
            @PathVariable Long propertyId,
            @Valid @RequestBody com.stayhub.backend.Module.Property.DTO.Request.PropertyApprovalRequest request) {

        propertyService.reviewProperty(propertyId, request);

        return ResponseEntity.ok(new ResponseData<>(200, "Cập nhật trạng thái bài đăng thành công", null));
    }

    @Operation(summary = "ADMIN - Lấy danh sách tất cả các gói cước")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/subscription-plans")
    public ResponseEntity<ResponseData<List<SubscriptionPlanResponse>>> getAllSubscriptionPlans() {
        List<SubscriptionPlanResponse> response = subscriptionService.getAllSubscriptionPlans();
        return ResponseEntity.ok(new ResponseData<>(200, "Lấy danh sách gói cước thành công", response));
    }
}
