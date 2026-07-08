package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Common.Util.UserSubscriptionStatus;
import com.stayhub.backend.Module.Property.Model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findFirstByUser_IdAndStatusOrderByStartDateDesc(Long userId, UserSubscriptionStatus status);
}
