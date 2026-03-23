package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Module.Property.Model.CancellationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy,Long> {
}
