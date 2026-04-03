package com.stayhub.backend.Module.Property.DTO.Response;

import java.util.List;

public record TopCategoryPropertiesResponse(
        String categoryName,
        List<PropertyCardResponse> properties
) {
}
