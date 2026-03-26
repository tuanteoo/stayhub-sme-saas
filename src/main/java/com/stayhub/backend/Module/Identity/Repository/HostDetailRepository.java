package com.stayhub.backend.Module.Identity.Repository;

import com.stayhub.backend.Module.Identity.Model.HostDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostDetailRepository extends JpaRepository<HostDetail,Long> {
    boolean existsByHostCode(String hostCode);
}
