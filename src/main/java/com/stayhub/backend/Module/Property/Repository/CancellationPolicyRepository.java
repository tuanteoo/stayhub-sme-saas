package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Module.Property.Model.CancellationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy,Long> {
    List<CancellationPolicy> findByIsActiveTrue();
}
