package com.stayhub.backend.Module.Property.Service;

import com.stayhub.backend.Module.Property.DTO.Response.MySubscriptionResponse;
import com.stayhub.backend.Module.Property.DTO.Response.SubscriptionPlanResponse;

import java.util.List;

public interface SubscriptionService {
    MySubscriptionResponse getMySubscription(Long hostId);
    List<SubscriptionPlanResponse> getActiveSubscriptionPlans();
    void processSubscriptionPurchase(Long hostId, Long newPlanId);
}
