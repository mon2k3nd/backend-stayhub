package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "owner_tenant_mappings")
@Data
public class OwnerTenantMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long ownerId;
    private Long roomId;
    private Long tenantUserId;
    private String tenantPhoneNumber;
}