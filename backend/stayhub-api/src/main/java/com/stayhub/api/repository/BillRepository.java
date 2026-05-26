package com.stayhub.api.repository;

import com.stayhub.api.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByRoomIdOrderByYearDescMonthDesc(Long roomId);

    List<Bill> findByIsPaidFalse();

    // FIX: 1 query cho tất cả phòng thay vì N queries (N+1 fix)
    List<Bill> findByRoomIdIn(List<Long> roomIds);

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.roomId IN :roomIds AND b.isPaid = false")
    long countUnpaidByRoomIdIn(@Param("roomIds") List<Long> roomIds);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.roomId IN :roomIds AND b.isPaid = true")
    double sumRevenueByRoomIdIn(@Param("roomIds") List<Long> roomIds);

    boolean existsByRoomIdAndMonthAndYear(Long roomId, Integer month, Integer year);
}