package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Module.Property.Model.RentalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalTypeRepository extends JpaRepository<RentalType,Long> {
}
