package com.stayhub.backend.Module.Booking.Service;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Module.Booking.DTO.Request.DisputeCreateRequest;
import com.stayhub.backend.Module.Booking.DTO.Response.DisputeAdminResponse;
import com.stayhub.backend.Module.Booking.Model.Dispute;

public interface DisputeService {
    String createDispute(Long userId,String bookingCode,DisputeCreateRequest request);
    PageResponse<DisputeAdminResponse> getDisputesForAdmin(String status, int pageNo, int pageSize, String sortBy, String sortDir);
}
