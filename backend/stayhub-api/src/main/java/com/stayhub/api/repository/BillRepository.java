package com.stayhub.api.repository;

import com.stayhub.api.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByRoomIdOrderByYearDescMonthDesc(Long roomId);
    List<Bill> findByIsPaidFalse();
}