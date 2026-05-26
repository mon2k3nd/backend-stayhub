package com.stayhub.api.repository;

import com.stayhub.api.entity.Roommate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoommateRepository extends JpaRepository<Roommate, Long> {
    List<Roommate> findByContractIdAndIsActiveTrue(Long contractId);
    List<Roommate> findByRoomIdAndIsActiveTrue(Long roomId);
    int countByContractIdAndIsActiveTrue(Long contractId);
}
