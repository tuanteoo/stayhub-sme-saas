package com.stayhub.backend.Module.Property.Service;

import com.stayhub.backend.Module.Property.DTO.Response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
}
