package com.stayhub.backend.Module.Property.Service;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Module.Identity.DTO.Response.PropertyDetailResponse;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import com.stayhub.backend.Module.Property.DTO.Response.PropertyCardResponse;

public interface PropertyService {
    void createProperty(String hostEmail, PropertyCreateRequest request);
    PageResponse<PropertyCardResponse> getPropertiesForGuest(int page, int size, String sortBy, String sortDir, String destination, Integer guestCount);
    PropertyDetailResponse getPropertyBySlug(String slug);
}
