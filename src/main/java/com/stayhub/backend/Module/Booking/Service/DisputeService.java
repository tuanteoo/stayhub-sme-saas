package com.stayhub.backend.Module.Booking.Service;

import com.stayhub.backend.Module.Booking.DTO.Request.DisputeCreateRequest;
import com.stayhub.backend.Module.Booking.Model.Dispute;

public interface DisputeService {
    String createDispute(Long userId,String bookingCode,DisputeCreateRequest request);
}
