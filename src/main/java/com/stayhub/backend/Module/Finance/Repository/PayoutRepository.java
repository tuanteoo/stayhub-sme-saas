package com.stayhub.backend.Module.Finance.Repository;

import com.stayhub.backend.Common.Util.PayoutStatus;
import com.stayhub.backend.Module.Finance.Model.Payout;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutRepository extends JpaRepository<Payout, Long> {
    @NonNull
    @Override
    @EntityGraph(attributePaths = {"user", "user.profile"})
    Page<Payout> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "user.profile"})
    Page<Payout> findByStatus(PayoutStatus status, Pageable pageable);
}
