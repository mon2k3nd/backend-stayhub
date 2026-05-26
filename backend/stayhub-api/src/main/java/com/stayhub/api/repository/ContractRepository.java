package com.stayhub.api.repository;

import com.stayhub.api.entity.Contract;
import com.stayhub.api.entity.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByTenantId(Long tenantId);
    List<Contract> findByOwnerId(Long ownerId);
    List<Contract> findByRoomIdAndStatus(Long roomId, ContractStatus status);
    Optional<Contract> findByRoomIdAndStatusIn(Long roomId, List<ContractStatus> statuses);

    // Hợp đồng sắp hết hạn (trong vòng 30 ngày)
    @Query("SELECT c FROM Contract c WHERE c.status = 'ACTIVE' AND c.endDate BETWEEN :today AND :in30Days")
    List<Contract> findExpiringContracts(LocalDate today, LocalDate in30Days);
}
