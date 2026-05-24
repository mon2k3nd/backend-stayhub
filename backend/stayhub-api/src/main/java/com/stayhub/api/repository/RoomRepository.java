package com.stayhub.api.repository;

import com.stayhub.api.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByOwnerId(Long ownerId);
    long countByOwnerId(Long ownerId);
}