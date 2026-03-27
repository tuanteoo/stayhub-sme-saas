package com.stayhub.backend.Module.Property.Service;

import com.stayhub.backend.Module.Property.DTO.Request.RentalTypeBulkRequest;
import com.stayhub.backend.Module.Property.DTO.Response.RentalTypeResponse;

import java.util.List;

public interface RentalTypeService {
    void bulkCreateRentalTypes(List<RentalTypeBulkRequest> requests);
    List<RentalTypeResponse> getAllRentalTypes();
}
