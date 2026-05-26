package com.stayhub.api.repository;

import com.stayhub.api.entity.MaintenanceRequest;
import com.stayhub.api.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    @Query("""
SELECT m FROM MaintenanceRequest m 
WHERE m.ownerId = :ownerId
AND (:status IS NULL OR m.status = :status)
AND (
    :search IS NULL 
    OR LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%'))
    OR LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%'))
)
""")
    List<MaintenanceRequest> searchAndFilterForOwner(
                    @Param("ownerId") Long ownerId,
                    @Param("status") RequestStatus status,
                    @Param("search") String search
            );

    // Lấy danh sách công việc được giao cho nhân viên (Staff)
    List<MaintenanceRequest> findByStaffIdAndStatusIn(Long staffId, List<RequestStatus> statuses);

    // Lấy danh sách lịch sử sự cố riêng của một phòng (Tenant View)
    List<MaintenanceRequest> findByRoomIdOrderByCreatedAtDesc(Long roomId);
}