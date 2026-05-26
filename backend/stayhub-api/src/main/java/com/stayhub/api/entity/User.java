package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "email", unique = true, nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'no-email@stayhub.com'")
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role_id")
    @Builder.Default
    private String roleId = "TENANT";

    @Column(name = "package_id")
    @Builder.Default
    private String packageId = "FREE";

    // ACTIVE | LOCKED | PENDING_KYC
    @Column(name = "account_status")
    @Builder.Default
    private String accountStatus = "ACTIVE";

    @Column(name = "is_requesting_owner")
    @Builder.Default
    private boolean isRequestingOwner = false;

    @Column(name = "cccd_number")
    private String cccdNumber;

    @Column(name = "cccd_front_url")
    private String cccdFrontUrl;

    @Column(name = "cccd_back_url")
    private String cccdBackUrl;

    @Column(name = "hometown")
    private String hometown;

    @Column(name = "gender")
    private String gender;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "fcm_token")
    private String fcmToken;
}
