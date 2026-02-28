package com.stayhub.backend.Module.Identity.Repository;

import com.stayhub.backend.Module.Identity.Model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
