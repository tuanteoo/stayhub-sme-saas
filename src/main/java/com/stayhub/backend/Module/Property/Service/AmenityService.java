package com.stayhub.backend.Module.Property.Service;

import com.stayhub.backend.Module.Property.DTO.Request.AmenityRequest;
import com.stayhub.backend.Module.Property.DTO.Response.AmenityResponse;

import java.util.List;

public interface AmenityService {
    AmenityResponse createAmenity(AmenityRequest request);
    List<AmenityResponse> getAllAmenities();
}
