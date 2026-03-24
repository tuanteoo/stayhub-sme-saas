package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Common.Util.PropertyStatus;
import com.stayhub.backend.Module.Property.Model.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PropertyRepository extends JpaRepository<Property,Long>, JpaSpecificationExecutor<Property> {
    Page<Property> findByStatus(PropertyStatus status, Pageable pageable);
}
