package com.stayhub.backend.Module.Property.Service;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Module.Property.DTO.Response.*;
import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;

import java.time.LocalDate;
import java.util.List;

public interface PropertyService {
    void createProperty(Long hostId, PropertyCreateRequest request, boolean isFirstPropertyOnboarding);
    PageResponse<HostPropertyResponse> getPropertiesByHost(Long id, int page, int size, String sortBy, String sortDir);
    PageResponse<PropertyCardResponse> getPropertiesForGuest(int page, int size, String sortBy, String sortDir, String destination, Integer guestCount, LocalDate checkInDate, LocalDate checkOutDate, String categorySlug);
    PropertyDetailResponse getPropertyBySlug(String slug, LocalDate checkInDate, LocalDate checkOutDate);
    List<RoomPriceResponse> calculatePriceForProperty(String slug, LocalDate checkInDate, LocalDate checkOutDate, List<Long> roomIds);
    void approveFirstPendingPropertyByHost(Long id);
    void reviewProperty(Long propertyId, com.stayhub.backend.Module.Property.DTO.Request.PropertyApprovalRequest request);
    List<PropertyCardResponse> getTopPropertiesByCategorySlug(String categorySlug);
}
