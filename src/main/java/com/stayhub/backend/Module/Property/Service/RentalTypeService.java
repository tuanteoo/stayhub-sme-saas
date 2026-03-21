package com.stayhub.backend.Module.Property.Service;

import com.stayhub.backend.Module.Property.DTO.Response.RentalTypeResponse;

import java.util.List;

public interface RentalTypeService {
    List<RentalTypeResponse> getAllRentalTypes();
}
