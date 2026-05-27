package com.stayhub.api.repository;

import com.stayhub.api.entity.Room;
import com.stayhub.api.entity.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByOwnerId(Long ownerId);
    Page<Room> findByOwnerId(Long ownerId, Pageable pageable);

    default Page<Room> findByOwnerIdPaged(Long ownerId, Pageable pageable) {
        return findByOwnerId(ownerId, pageable);
    }

    List<Room> findByOwnerIdAndStatus(Long ownerId, RoomStatus status);
    List<Room> findByBranchId(Long branchId);
    Optional<Room> findByCurrentTenantId(Long tenantId);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.ownerId = :ownerId AND r.status = 'DA_THUE'")
    long countOccupied(Long ownerId);
}
