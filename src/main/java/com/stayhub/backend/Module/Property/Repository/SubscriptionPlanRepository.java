package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Module.Property.Model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findAllByIsActiveTrueOrderByPriceAsc();
}
