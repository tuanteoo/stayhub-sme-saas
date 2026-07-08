package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Module.Property.Model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    boolean existsByName(String name);
    List<Amenity> findByIdIn(List<Long> ids);
}
