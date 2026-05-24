package com.stayhub.api.repository;

import com.stayhub.api.entity.CashCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CashCollectionRepository extends JpaRepository<CashCollection, Long> {
    List<CashCollection> findByStaffIdAndIsHandedOverToOwnerFalse(Long staffId); // Sửa lỗi hàm đối soát
}