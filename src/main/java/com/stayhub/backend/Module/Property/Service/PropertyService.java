package com.stayhub.backend.Module.Property.Service;

import com.stayhub.backend.Module.Property.DTO.Request.PropertyCreateRequest;

public interface PropertyService {
    void createProperty(String hostEmail, PropertyCreateRequest request);
}
