package com.stayhub.api.repository;

import com.stayhub.api.entity.MaintenanceRequest;
import com.stayhub.api.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    // Tìm danh sách báo hỏng thuộc cụm nhà của một Chủ nhà
    List<MaintenanceRequest> findByOwnerId(Long ownerId);

    // Tìm danh sách công việc sửa chữa được giao cho một Nhân viên
    List<MaintenanceRequest> findByStaffId(Long staffId);

    // Tìm lịch sử báo hỏng của riêng một Khách thuê
    List<MaintenanceRequest> findByTenantId(Long tenantId);

    // Hàm tìm kiếm bổ trợ kết hợp trạng thái (Dùng cho logic Service nếu cần)
    List<MaintenanceRequest> findByOwnerIdAndStatus(Long ownerId, RequestStatus status);
}