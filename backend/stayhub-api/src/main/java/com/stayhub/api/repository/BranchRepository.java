package com.stayhub.api.repository;

import com.stayhub.api.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByOwnerIdAndIsActiveTrue(Long ownerId);
    List<Branch> findByOwnerId(Long ownerId);
    long countByOwnerId(Long ownerId);
}
