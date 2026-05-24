package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "owner_tenant_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerTenantMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerId;
    private Long roomId;          // Sửa lỗi setRoomId()
    private String tenantPhoneNumber; // Sửa lỗi setTenantPhoneNumber()
    private Long tenantUserId;     // Sửa lỗi setTenantUserId() và getTenantUserId()
}