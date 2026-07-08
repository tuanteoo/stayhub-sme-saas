package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Module.Property.Model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room,Long> {
    List<Room> findAllByIsActiveTrue();
}
