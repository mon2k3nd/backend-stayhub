package com.stayhub.api.repository;

import com.stayhub.api.entity.OwnerTenantMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OwnerTenantMappingRepository extends JpaRepository<OwnerTenantMapping, Long> {
    List<OwnerTenantMapping> findByTenantPhoneNumber(String phoneNumber); // Sửa lỗi hàm
    List<OwnerTenantMapping> findByRoomId(Long roomId); // Sửa lỗi hàm
}