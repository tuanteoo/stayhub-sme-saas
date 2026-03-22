package com.stayhub.backend.Module.Property.Controller;

import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Property.DTO.Response.CategoryResponse;
import com.stayhub.backend.Module.Property.DTO.Response.RentalTypeResponse;
import com.stayhub.backend.Module.Property.Service.RentalTypeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RentalTypeController {
    private final RentalTypeService rentalTypeService;

    @Operation(summary = "Lấy danh sách loại hình cho thuê")
    @GetMapping("/public/rental-types")
    public ResponseData<List<RentalTypeResponse>> getAllRentalTypes() {
        List<RentalTypeResponse> response = rentalTypeService.getAllRentalTypes();
        return new ResponseData<>(200, "Lấy danh sách loại hình cho thuê thành công", response);
    }
}
