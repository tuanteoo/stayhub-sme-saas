package com.stayhub.backend.Module.Property.Service;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Module.Property.DTO.Response.PropertyDetailResponse;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;
import com.stayhub.backend.Module.Property.DTO.Response.PropertyCardResponse;

import java.time.LocalDate;

public interface PropertyService {
    void createProperty(String email, PropertyCreateRequest request);
    PageResponse<PropertyCardResponse> getPropertiesForGuest(int page, int size, String sortBy, String sortDir, String destination, Integer guestCount, LocalDate checkInDate, LocalDate checkOutDate);
    PropertyDetailResponse getPropertyBySlug(String slug);
    void approveFirstPendingPropertyByHost(Long id);
}
