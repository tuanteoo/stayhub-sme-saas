package com.stayhub.backend.Common.Mapper;

import com.stayhub.backend.Module.Property.DTO.Response.AmenityResponse;
import com.stayhub.backend.Module.Property.Model.Amenity;
import org.springframework.stereotype.Component;

@Component
public class AmenityMapper {
    public AmenityResponse toResponse(Amenity amenity) {
        if (amenity == null) return null;
        return new AmenityResponse(
                amenity.getId(),
                amenity.getName(),
                amenity.getIconName(),
                amenity.getType()
        );
    }
}
