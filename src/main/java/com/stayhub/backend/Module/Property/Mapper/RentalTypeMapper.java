package com.stayhub.backend.Module.Property.Mapper;

import com.stayhub.backend.Module.Property.DTO.Response.CategoryResponse;
import com.stayhub.backend.Module.Property.DTO.Response.RentalTypeResponse;
import com.stayhub.backend.Module.Property.Model.Category;
import com.stayhub.backend.Module.Property.Model.RentalType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RentalTypeMapper {
    private final CategoryMapper categoryMapper;

    public RentalTypeResponse toResponse(RentalType rentalType) {
        if (rentalType == null) return null;

        List<CategoryResponse> categories = rentalType.getCategories().stream()
                .filter(Category::getIsActive)
                .map(categoryMapper::toResponse)
                .toList();

        return RentalTypeResponse.builder()
                .id(rentalType.getId())
                .name(rentalType.getName())
                .slug(rentalType.getSlug())
                .description(rentalType.getDescription())
                .iconName(rentalType.getIconName())
                .categoryResponses(categories)
                .build();
    }
}
