package com.stayhub.api.repository;

import com.stayhub.api.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    long countByOwnerId(Long ownerId);

    // 🟢 ĐÃ SỬA: Chuyển từ existsByRoomNumberAndOwnerId thành existsByRoomNameAndOwnerId
    boolean existsByRoomNameAndOwnerId(String roomName, Long ownerId);
}