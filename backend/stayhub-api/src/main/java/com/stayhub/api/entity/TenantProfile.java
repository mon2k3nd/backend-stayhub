package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "tenant_profiles") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TenantProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tenantCode; // Công thức cố định: [Mã_Tòa]-[Số_Phòng]-[STT]

    private Long roomId;
    private Long ownerId;

    private String fullName;
    private String phoneNumber;
    private String cccdNumber;
    private String vehiclePlate; // Biển số xe
    private Integer age;
    private String hometown;
    private String job;
    private String gender;

    public enum RequestStatus {
        PENDING,     // Yêu cầu mới gửi, chờ xử lý
        PROCESSING,  // Đang trong quá trình sửa chữa (Chủ nhà đã giao việc cho Staff)
        DONE,        // Đã sửa chữa xong hoàn toàn
        CANCELLED    // Yêu cầu bị hủy (Do bấm nhầm hoặc không cần thiết nữa)
    }
}