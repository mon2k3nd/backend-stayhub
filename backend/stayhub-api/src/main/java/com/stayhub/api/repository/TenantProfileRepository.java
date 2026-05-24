package com.stayhub.api.repository;

import com.stayhub.api.entity.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TenantProfileRepository extends JpaRepository<TenantProfile, Long> {
    Optional<TenantProfile> findByPhoneNumber(String phoneNumber); // Sửa lỗi hàm
    List<TenantProfile> findByOwnerId(Long ownerId);
}