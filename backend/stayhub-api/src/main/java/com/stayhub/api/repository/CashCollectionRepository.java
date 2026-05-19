package com.stayhub.api.repository;
import com.stayhub.api.entity.CashCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CashCollectionRepository extends JpaRepository<CashCollection, Long> {
    List<CashCollection> findByStaffIdAndIsHandedOverToOwner(Long staffId, boolean status);
}