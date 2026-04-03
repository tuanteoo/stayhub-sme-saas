package com.stayhub.backend.Module.Property.Service.Implement;

import com.stayhub.backend.Module.Property.DTO.Response.CategoryResponse;
import com.stayhub.backend.Module.Property.Mapper.CategoryMapper;
import com.stayhub.backend.Module.Property.Repository.CategoryRepository;
import com.stayhub.backend.Module.Property.Service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
}
