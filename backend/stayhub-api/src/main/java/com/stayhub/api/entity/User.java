package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role; // ADMIN, OWNER, STAFF, TENANT

    // ===== DÀNH RIÊNG CHO OWNER (ADMIN kiểm soát gói) =====
    @Enumerated(EnumType.STRING)
    private PlanType currentPlan;
    private Integer roomLimit;
    private Boolean isApprovedByAdmin;

    // ===== 🌟 BỔ SUNG TRƯỜNG NÀY ĐỂ FIX LỖI CHO STAFF =====
    private Long ownerId; // Nếu role là STAFF, trường này lưu ID của Chủ nhà quản lý nhân viên này
}