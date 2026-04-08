package com.stayhub.backend.Module.Property.Service.Implement;

import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.UserSubscriptionStatus;
import com.stayhub.backend.Module.Identity.Repository.HostDetailRepository;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import com.stayhub.backend.Module.Property.DTO.Response.MySubscriptionResponse;
import com.stayhub.backend.Module.Property.DTO.Response.SubscriptionPlanResponse;
import com.stayhub.backend.Module.Property.Model.SubscriptionPlan;
import com.stayhub.backend.Module.Property.Model.UserSubscription;
import com.stayhub.backend.Module.Property.Repository.SubscriptionPlanRepository;
import com.stayhub.backend.Module.Property.Repository.UserSubscriptionRepository;
import com.stayhub.backend.Module.Property.Service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final HostDetailRepository hostDetailRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;

    @Override
    public MySubscriptionResponse getMySubscription(Long hostId) {
        if (!hostDetailRepository.existsById(hostId)) {
            throw new ResourceNotFoundException("Tài khoản của bạn chưa được thiết lập hồ sơ Chủ nhà.");
        }

        UserSubscription subscription = userSubscriptionRepository
                .findFirstByUser_IdAndStatusOrderByStartDateDesc(hostId, UserSubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói cước đang hoạt động."));

        return MySubscriptionResponse.builder()
                .subscriptionId(subscription.getId())
                .tier(subscription.getPlan().getTier())
                .planName(subscription.getPlan().getName())
                .commissionRate(subscription.getCurrentCommissionRate())
                .maxListings(subscription.getCurrentMaxListings())
                .creditLimit(subscription.getCurrentCreditLimit())
                .build();
    }

    @Override
    public List<SubscriptionPlanResponse> getActiveSubscriptionPlans() {
        return subscriptionPlanRepository.findAllByIsActiveTrueOrderByPriceAsc()
                .stream()
                .map(plan -> SubscriptionPlanResponse.builder()
                        .id(plan.getId())
                        .name(plan.getName())
                        .description(plan.getDescription())
                        .tier(plan.getTier())
                        .price(plan.getPrice())
                        .durationMonths(plan.getDurationMonths())
                        .maxListings(plan.getMaxListings())
                        .commissionRate(plan.getCommissionRate())
                        .creditLimit(plan.getCreditLimit())
                        .build())
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processSubscriptionPurchase(Long hostId, Long newPlanId) {
        Optional<UserSubscription> currentActiveSub = userSubscriptionRepository
                .findFirstByUser_IdAndStatusOrderByStartDateDesc(hostId, UserSubscriptionStatus.ACTIVE);

        if (currentActiveSub.isPresent()) {
            UserSubscription oldSub = currentActiveSub.get();
            oldSub.setStatus(UserSubscriptionStatus.UPGRADED);
            oldSub.setEndDate(LocalDateTime.now());
            userSubscriptionRepository.save(oldSub);
        }

        SubscriptionPlan newPlan = subscriptionPlanRepository.findById(newPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Gói cước không tồn tại"));

        UserSubscription newSub = UserSubscription.builder()
                .user(userRepository.getReferenceById(hostId))
                .plan(newPlan)
                .status(UserSubscriptionStatus.ACTIVE)
                .autoRenew(true)
                .currentCommissionRate(newPlan.getCommissionRate())
                .currentCreditLimit(newPlan.getCreditLimit())
                .currentMaxListings(newPlan.getMaxListings())
                .endDate(newPlan.getDurationMonths() != null ? LocalDateTime.now().plusMonths(newPlan.getDurationMonths()) : null)
                .build();

        userSubscriptionRepository.save(newSub);
    }
}
