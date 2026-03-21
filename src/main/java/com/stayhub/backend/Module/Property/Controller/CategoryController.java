package com.stayhub.backend.Module.Property.Controller;

import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Property.DTO.Request.CategoryRequest;
import com.stayhub.backend.Module.Property.DTO.Response.CategoryResponse;
import com.stayhub.backend.Module.Property.Service.CategoryService;
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
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/admin/categories")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseData<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return new ResponseData<>(201, "Tạo danh mục thành công", response);
    }

    @GetMapping("/public/categories")
    public ResponseData<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> response = categoryService.getAllActiveCategories();
        return new ResponseData<>(200, "Lấy danh sách danh mục thành công", response);
    }
}
