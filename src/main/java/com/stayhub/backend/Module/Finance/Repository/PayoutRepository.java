package com.stayhub.backend.Module.Finance.Repository;

import com.stayhub.backend.Module.Finance.Model.Payout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutRepository extends JpaRepository<Payout, Long> {
}
