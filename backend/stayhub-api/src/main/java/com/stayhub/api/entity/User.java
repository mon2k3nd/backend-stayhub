package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "users") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    private String fullName;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role;

    // SaaS Multi-tenant & Pricing Matrix
    @Enumerated(EnumType.STRING)
    private PlanType currentPlan;
    private Integer roomLimit;
    private LocalDateTime planExpiredAt;

    private Long ownerId; // Định danh dữ liệu thuộc về chủ nhà nào
}