package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId; // ID của khách thuê gửi yêu cầu
    private Long roomId;   // ID phòng xảy ra sự cố
    private Long ownerId;  // ID chủ nhà quản lý (dùng để lọc danh sách nhanh cho Owner)
    private Long staffId;  // ID nhân viên được giao đi sửa (có thể null nếu chưa giao)

    private String title;        // Tiêu đề (Ví dụ: Hỏng điều hòa)
    private String description;  // Mô tả chi tiết tình trạng
    private String imageUrl;     // Đường dẫn ảnh chụp sự cố (nếu có)

    @Enumerated(EnumType.STRING)
    private RequestStatus status; // PENDING, PROCESSING, DONE, CANCELLED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}