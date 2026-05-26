package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "users")
@Getter // Tự động sinh toàn bộ Getters
@Setter // Tự động sinh toàn bộ Setters
@NoArgsConstructor // Tự động sinh Constructor không tham số bắt buộc của JPA
@AllArgsConstructor // Tự động sinh Constructor đầy đủ tham số cho Builder
@Builder // Hỗ trợ khởi tạo Object theo Design Pattern Builder mượt mà
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

    @Column(name = "is_requesting_owner")
    @Builder.Default
    private boolean isRequestingOwner = false; // Đã cấu hình Builder.Default để loại bỏ cảnh báo Redundant

    @Column(name = "cccd_number", nullable = true)
    private String cccdNumber;

    @Column(name = "hometown", nullable = true)
    private String hometown;

    @Column(name = "gender", nullable = true)
    private String gender;
}