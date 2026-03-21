package com.stayhub.backend.Module.Property.Service.Implement;

import com.stayhub.backend.Module.Property.DTO.Response.RentalTypeResponse;
import com.stayhub.backend.Module.Property.Model.RentalType;
import com.stayhub.backend.Module.Property.Repository.RentalTypeRepository;
import com.stayhub.backend.Module.Property.Service.RentalTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalTypeServiceImpl implements RentalTypeService {
    private final RentalTypeRepository rentalTypeRepository;

    @Override
    public List<RentalTypeResponse> getAllRentalTypes() {
        return rentalTypeRepository.findAll().stream()
                .filter(RentalType::getIsActive)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RentalTypeResponse mapToResponse(RentalType rentalType) {
        return RentalTypeResponse.builder()
                .id(rentalType.getId())
                .name(rentalType.getName())
                .slug(rentalType.getSlug())
                .description(rentalType.getDescription())
                .iconName(rentalType.getIconName())
                .isActive(rentalType.getIsActive())
                .build();
    }
}
