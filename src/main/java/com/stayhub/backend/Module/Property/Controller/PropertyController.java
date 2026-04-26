package com.stayhub.backend.Module.Property.Controller;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Identity.Security.CustomUserDetails;
import com.stayhub.backend.Module.Property.DTO.Request.CalendarUpdateRequest;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyApprovalRequest;
import com.stayhub.backend.Module.Property.DTO.Response.*;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import com.stayhub.backend.Module.Property.Service.*;
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
    private final RoomService roomService;

    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @Operation(summary = "HOST - Tạo bài đăng")
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
            @Parameter(
                    name = "status",
                    description = "lọc theo trạng thái (DRAFT, PENDING_REVIEW, ACTIVE, INACTIVE, HIDDEN, BANNED, REJECTED)"
            )
            @RequestParam(required = false) String status,
            @Parameter(
                    name = "searchTerm",
                    description = "Tìm kiếm theo tên hoặc địa chỉ (VD: inter, Hà Nội, han,...)"
            )
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponse<HostPropertyResponse> response = propertyService.getPropertiesByHost(
                customUserDetails.getUser().getId(), status,searchTerm, page, size, sortBy, sortDir);

        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Lấy danh sách bài đăng của Host thành công", response));
    }

    @Operation(summary = "GUEST - Lấy 8 bài đăng nổi bật theo category slug")
    @GetMapping("/category/{slug}/top")
    public ResponseEntity<ResponseData<List<PropertyCardResponse>>> getTopPropertiesByCategorySlug(
            @Parameter(
                    name = "categorySlug",
                    description = "Slug của danh mục - xem ở bảng categories"
            )
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
            @Parameter(
                    name = "destination",
                    description = "Điểm đến, VD: HaNoi, han,.."
            )
            @RequestParam(required = false) String destination,
            @Parameter(
                    name = "guestCount",
                    description = "Số lượng khách để lọc bài đăng phù hợp"
            )
            @RequestParam(required = false) Integer guestCount,

            @Parameter(
                    name = "checkInDate",
                    description = "Ngày nhận phòng, định dạng yyyy-MM-dd"
            )
            @RequestParam(required = false) LocalDate checkInDate,
            @Parameter(
                    name = "checkOutDate",
                    description = "Ngày trả phòng, định dạng yyyy-MM-dd"
            )
            @RequestParam(required = false) LocalDate checkOutDate,
            @Parameter(
                    name = "categorySlug",
                    description = "Slug của danh mục - xem ở bảng categories"
            )
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
            @Parameter(
                    name = "slug",
                    description = "Slug của bài đăng - xem ở bảng properties"
            )
            @PathVariable String slug,
            @Parameter(
                    name = "checkInDate",
                    description = "Ngày nhận phòng, định dạng yyyy-MM-dd"
            )
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @Parameter(
                    name = "checkOutDate",
                    description = "Ngày trả phòng, định dạng yyyy-MM-dd"
            )
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {

        PropertyDetailResponse propertyDetail = propertyService.getPropertyBySlug(slug, checkInDate, checkOutDate);

        return ResponseEntity.ok(new ResponseData<>(200, "Lấy thông tin chi tiết thành công", propertyDetail));
    }

    @Operation(summary = "GUEST_USER - Tính tiền cho phòng đã chọn dựa trên slug bài đăng, ngày nhận phòng và ngày trả phòng")
    @GetMapping("/{slug}/calculate-price")
    public ResponseEntity<ResponseData<List<RoomPriceResponse>>> calculatePriceBySlug(
            @Parameter(
                    name = "slug",
                    description = "Slug của bài đăng - xem ở bảng properties"
            )
            @PathVariable String slug,
            @Parameter(
                    name = "checkInDate",
                    description = "Ngày nhận phòng, định dạng yyyy-MM-dd"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @Parameter(
                    name = "checkOutDate",
                    description = "Ngày trả phòng, định dạng yyyy-MM-dd"
            )
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
            @Parameter(
                    name = "propertyId",
                    description = "ID của bài đăng cần thẩm định - xem ở bảng properties"
            )
            @PathVariable Long propertyId,
            @Valid @RequestBody PropertyApprovalRequest request) {

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

    @Operation(summary = "HOST - Thống kê tổng quan và kiểm tra hạn mức bài đăng")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @GetMapping("/hosts/stats")
    public ResponseEntity<ResponseData<HostPropertyStatsResponse>> getMyDashboardStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long hostId = userDetails.getUser().getHostDetail().getId();
        HostPropertyStatsResponse response = propertyService.getHostPropertyStats(hostId);

        return ResponseEntity.ok(new ResponseData<>(200, "Lấy thống kê thành công", response));
    }

    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @Operation(summary = "HOST - Bật/tắt trạng thái hoạt động của bài đăng (ACTIVE/INACTIVE)")
    @PutMapping("/host/{propertyId}/toggle-status")
    public ResponseEntity<ResponseData<String>> togglePropertyStatus(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(
                    name = "propertyId",
                    description = "ID của bài đăng - xem ở bảng properties")
            @PathVariable Long propertyId,
            @Parameter(
                    name = "isActive",
                    description = "true: Mở hoạt động (ACTIVE), false: Tạm ngưng (INACTIVE)")
            @RequestParam boolean isActive) {

        propertyService.togglePropertyStatus(customUserDetails.getUser().getId(), propertyId, isActive);

        String message = isActive
                ? "Đã mở lại hoạt động cho bài đăng thành công."
                : "Đã tạm ngưng bài đăng thành công.";

        return ResponseEntity.ok(new ResponseData<>(200, message));
    }

    @Operation(summary = "HOST - Xem lịch phòng theo tháng")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @GetMapping("/host/rooms/{roomId}/calendar")
    public ResponseEntity<ResponseData<List<CalendarDayResponse>>> getRoomCalendar(
            @Parameter(description = "ID của phòng") @PathVariable Long roomId,
            @Parameter(description = "Năm cần xem (VD: 2024)") @RequestParam int year,
            @Parameter(description = "Tháng cần xem (VD: 12)") @RequestParam int month,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        List<CalendarDayResponse> response = roomService.getRoomCalendar(customUserDetails.getUser().getId(), roomId, year, month);
        return ResponseEntity.ok(new ResponseData<>(200, "Lấy dữ liệu lịch phòng thành công.", response));
    }

    @Operation(summary = "HOST - Cập nhật lịch phòng (Khóa phòng, đổi giá theo ngày)")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PutMapping("/host/rooms/{roomId}/calendar")
    public ResponseEntity<ResponseData<Void>> updateRoomCalendar(
            @Parameter(description = "ID của phòng") @PathVariable Long roomId,
            @Valid @RequestBody CalendarUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        roomService.updateRoomCalendar(customUserDetails.getUser().getId(), roomId, request);
        return ResponseEntity.ok(new ResponseData<>(200, "Cập nhật lịch phòng thành công."));
    }
}
