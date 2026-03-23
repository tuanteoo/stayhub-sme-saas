package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Module.Property.Model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion,Long> {
    boolean existsByCode(String code);
}
