package com.stayhub.backend.Module.Property.Service.Implement;

import com.stayhub.backend.Common.Util.SlugUtils;
import com.stayhub.backend.Module.Property.DTO.Request.CategoryRequest;
import com.stayhub.backend.Module.Property.DTO.Request.RentalTypeBulkRequest;
import com.stayhub.backend.Module.Property.DTO.Response.CategoryResponse;
import com.stayhub.backend.Module.Property.DTO.Response.RentalTypeResponse;
import com.stayhub.backend.Common.Mapper.CategoryMapper;
import com.stayhub.backend.Module.Property.Model.Category;
import com.stayhub.backend.Module.Property.Model.RentalType;
import com.stayhub.backend.Module.Property.Repository.CategoryRepository;
import com.stayhub.backend.Module.Property.Repository.RentalTypeRepository;
import com.stayhub.backend.Module.Property.Service.RentalTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalTypeServiceImpl implements RentalTypeService {
    private final RentalTypeRepository rentalTypeRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public void bulkCreateRentalTypes(List<RentalTypeBulkRequest> requests) {
        Map<String, Category> categoryCache = new HashMap<>();

        for (RentalTypeBulkRequest req : requests) {
            RentalType rentalType = RentalType.builder()
                    .name(req.name())
                    .slug(SlugUtils.toSlug(req.name()))
                    .description(req.description())
                    .iconName(req.iconName())
                    .build();

            if (req.categories() != null) {
                for (CategoryRequest catReq : req.categories()) {
                    String catSlug = SlugUtils.toSlug(catReq.name());

                    Category category = categoryCache.get(catSlug);

                    if (category == null) {
                        category = categoryRepository.findBySlug(catSlug).orElse(null);
                    }

                    if (category == null) {
                        category = Category.builder()
                                .name(catReq.name())
                                .slug(catSlug)
                                .description(catReq.description())
                                .iconName(catReq.iconName())
                                .build();
                    }

                    categoryCache.put(catSlug, category);
                    rentalType.getCategories().add(category);
                }
            }

            rentalTypeRepository.save(rentalType);
        }
    }

    @Override
    public List<RentalTypeResponse> getAllRentalTypes() {
        return rentalTypeRepository.findAll().stream()
                .filter(RentalType::getIsActive)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RentalTypeResponse mapToResponse(RentalType rentalType) {
        List<CategoryResponse> categoryResponses = rentalType.getCategories().stream()
                .filter(Category::getIsActive)
                .map(categoryMapper::toResponse)
                .toList();

        return RentalTypeResponse.builder()
                .id(rentalType.getId())
                .name(rentalType.getName())
                .slug(rentalType.getSlug())
                .description(rentalType.getDescription())
                .iconName(rentalType.getIconName())
                .categoryResponses(categoryResponses)
                .build();
    }
}
