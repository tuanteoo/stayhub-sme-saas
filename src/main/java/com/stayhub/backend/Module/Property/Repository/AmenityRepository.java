package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Module.Property.Model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    boolean existsByName(String name);
}
