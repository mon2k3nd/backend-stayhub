package com.stayhub.api.repository;

import com.stayhub.api.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByRoomId(Long roomId);
    List<Bill> findByTenantId(Long tenantId);
    List<Bill> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    Optional<Bill> findByRoomIdAndMonthAndYear(Long roomId, int month, int year);

    @Query("SELECT b FROM Bill b WHERE b.isPaid = false AND b.tenantId = :tenantId ORDER BY b.year DESC, b.month DESC")
    List<Bill> findUnpaidByTenant(Long tenantId);

    @Query("SELECT b FROM Bill b WHERE b.isPaid = true AND b.month = :month AND b.year = :year")
    List<Bill> findPaidByMonthYear(int month, int year);
}
