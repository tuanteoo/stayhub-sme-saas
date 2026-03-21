package com.stayhub.backend.Module.Property.Service;

import com.stayhub.backend.Module.Property.DTO.Request.CategoryRequest;
import com.stayhub.backend.Module.Property.DTO.Response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    List<CategoryResponse> getAllActiveCategories();
}
