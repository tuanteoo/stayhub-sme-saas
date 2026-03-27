package com.stayhub.backend.Module.Property.Controller;

import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Property.DTO.Request.RentalTypeBulkRequest;
import com.stayhub.backend.Module.Property.DTO.Response.CategoryResponse;
import com.stayhub.backend.Module.Property.DTO.Response.RentalTypeResponse;
import com.stayhub.backend.Module.Property.Service.RentalTypeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RentalTypeController {
    private final RentalTypeService rentalTypeService;

    @Operation(summary = "Admin - Tạo hàng loạt loại hình và danh mục")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/admin/rental-types/bulk")
    public ResponseData<String> bulkCreateRentalTypes(@RequestBody @Valid List<RentalTypeBulkRequest> requests) {
        rentalTypeService.bulkCreateRentalTypes(requests);
        return new ResponseData<>(HttpStatus.CREATED.value(), "Tạo thành công dữ liệu Loại hình và Danh mục!");
    }

    @Operation(summary = "Lấy danh sách loại hình cho thuê và danh mục")
    @GetMapping("/public/rental-types")
    public ResponseData<List<RentalTypeResponse>> getAllRentalTypes() {
        List<RentalTypeResponse> response = rentalTypeService.getAllRentalTypes();
        return new ResponseData<>(200, "Lấy danh sách loại hình cho thuê thành công", response);
    }
}
