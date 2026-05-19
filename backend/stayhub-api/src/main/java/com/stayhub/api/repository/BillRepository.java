package com.stayhub.api.repository;
import com.stayhub.api.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByOwnerIdAndIsPaid(Long ownerId, boolean isPaid);
    List<Bill> findByIsPaid(boolean isPaid);
}
