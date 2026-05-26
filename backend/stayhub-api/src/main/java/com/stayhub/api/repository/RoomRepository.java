package com.stayhub.api.repository;

import com.stayhub.api.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByOwnerId(Long ownerId);

    long countByOwnerId(Long ownerId);

    boolean existsByRoomNameAndOwnerId(String roomName, Long ownerId);
}