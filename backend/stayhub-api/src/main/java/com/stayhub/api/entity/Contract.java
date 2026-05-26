package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "monthly_rent", nullable = false)
    private Double monthlyRent;

    @Column(name = "deposit_amount")
    private Double depositAmount;

    @Column(name = "electricity_price")
    private Double electricityPrice;

    @Column(name = "water_price")
    private Double waterPrice;

    @Column(name = "service_fee")
    private Double serviceFee;

    @Column(name = "terms", columnDefinition = "TEXT")
    private String terms;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ContractStatus status = ContractStatus.ACTIVE;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "terminated_at")
    private LocalDateTime terminatedAt;

    @Column(name = "termination_reason", columnDefinition = "TEXT")
    private String terminationReason;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
