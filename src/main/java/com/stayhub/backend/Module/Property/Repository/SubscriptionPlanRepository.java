package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Common.Util.SubscriptionTier;
import com.stayhub.backend.Module.Property.Model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findAllByIsActiveTrueOrderByPriceAsc();
    Optional<SubscriptionPlan> findByTier(SubscriptionTier tier);
    boolean existsByTier(@NonNull SubscriptionTier tier);
}
