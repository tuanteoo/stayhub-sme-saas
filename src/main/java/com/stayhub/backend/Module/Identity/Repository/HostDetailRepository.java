package com.stayhub.backend.Module.Identity.Repository;

import com.stayhub.backend.Common.Util.HostOnboardingStatus;
import com.stayhub.backend.Module.Identity.Model.HostDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HostDetailRepository extends JpaRepository<HostDetail,Long> {
    boolean existsByHostCode(String hostCode);
    Optional<HostDetail> findByHostCode(String hostCode);
    Page<HostDetail> findByOnboardingStatus(HostOnboardingStatus status, Pageable pageable);
}
