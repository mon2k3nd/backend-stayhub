package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "cash_collections") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CashCollection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long staffId;
    private Long roomId;
    private Long ownerId;
    private Double amountCollected;
    private LocalDateTime collectedAt;
    private boolean isHandedOverToOwner; // Đã bàn giao tiền mặt cho chủ nhà chưa
}