package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Module.Property.Model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property,Long> {
}
