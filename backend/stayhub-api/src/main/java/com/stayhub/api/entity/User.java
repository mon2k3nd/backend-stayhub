package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "email", unique = true)
    private String email;

    // nullable = false removed: bảng cũ đã có data, Hibernate không thể
    // thêm column NOT NULL vào table có sẵn data (PostgreSQL từ chối).
    // Constraint được đảm bảo bởi AuthService — mọi user mới đều có password.
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "role_id", nullable = false)
    @Builder.Default
    private String roleId = "TENANT";

    // nullable = false removed — cùng lý do như password_hash.
    // AuthService luôn set status = ACTIVE khi tạo user mới.
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Column(name = "account_status")
    private String accountStatus;

    @Column(name = "is_requesting_owner")
    @Builder.Default
    private boolean requestingOwner = false;

    @Column(name = "package_id")
    private String packageId;

    @Column(name = "cccd_number")
    private String cccdNumber;

    @Column(name = "hometown")
    private String hometown;

    @Column(name = "gender")
    private String gender;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "fcm_token")
    private String fcmToken;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum UserStatus {
        PENDING_VERIFICATION, ACTIVE, LOCKED
    }
}
