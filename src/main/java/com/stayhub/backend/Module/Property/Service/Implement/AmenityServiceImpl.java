package com.stayhub.backend.Module.Property.Service.Implement;

import com.stayhub.backend.Module.Property.DTO.Request.AmenityRequest;
import com.stayhub.backend.Module.Property.DTO.Response.AmenityResponse;
import com.stayhub.backend.Module.Property.Model.Amenity;
import com.stayhub.backend.Module.Property.Repository.AmenityRepository;
import com.stayhub.backend.Module.Property.Service.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AmenityServiceImpl implements AmenityService {
    private final AmenityRepository amenityRepository;

    @Override
    public AmenityResponse createAmenity(AmenityRequest request) {
        if (amenityRepository.existsByName(request.name())){
            throw new RuntimeException("Tên tiện ích đã tồn tại!");
        }

        Amenity amenity = Amenity.builder()
                .name(request.name())
                .iconName(request.iconName())
                .type(request.type())
                .build();
        amenityRepository.save(amenity);
        return mapToResponse(amenity);
    }

    @Override
    public List<AmenityResponse> getAllAmenities() {
        return amenityRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AmenityResponse mapToResponse(Amenity amenity) {
        return AmenityResponse.builder()
                .id(amenity.getId())
                .name(amenity.getName())
                .iconName(amenity.getIconName())
                .type(amenity.getType())
                .build();
    }
}
