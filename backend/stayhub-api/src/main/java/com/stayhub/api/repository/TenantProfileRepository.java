package com.stayhub.api.repository;
import com.stayhub.api.entity.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TenantProfileRepository extends JpaRepository<TenantProfile, Long> {
    List<TenantProfile> findByRoomId(Long roomId);
    List<TenantProfile> findByOwnerId(Long ownerId);
    long countByRoomId(Long roomId);
}