package com.stayhub.backend.Module.Property.Service.Implement;

import com.stayhub.backend.Common.Util.SlugUtils;
import com.stayhub.backend.Module.Property.DTO.Request.CategoryRequest;
import com.stayhub.backend.Module.Property.DTO.Response.CategoryResponse;
import com.stayhub.backend.Module.Property.Model.Category;
import com.stayhub.backend.Module.Property.Repository.CategoryRepository;
import com.stayhub.backend.Module.Property.Service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new RuntimeException("Tên danh mục đã tồn tại!");
        }

        String generatedSlug = SlugUtils.toSlug(request.name());

        Category category = Category.builder()
                .name(request.name())
                .slug(generatedSlug)
                .description(request.description())
                .iconName(request.iconName())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();

        categoryRepository.save(category);

        return mapToResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllActiveCategories() {
        return categoryRepository.findAll().stream()
                .filter(Category::getIsActive)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .iconName(category.getIconName())
                .isActive(category.getIsActive())
                .build();
    }
}
