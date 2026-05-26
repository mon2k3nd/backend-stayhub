package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_collections")
@Data
public class CashCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long billId;
    private Long roomId;
    private double amountCollected;

    // 🟢 ĐÃ SỬA: Đổi tên trường thành staffId để khớp với hàm trong CashCollectionRepository
    @Column(name = "collected_by_staff_id")
    private Long staffId;

    // 🟢 BỔ SUNG: Thêm trường này vì hàm trong Repository của bạn có "isHandedOverToOwnerFalse"
    @Column(name = "is_handed_over_to_owner", nullable = false)
    private boolean isHandedOverToOwner = false;

    private LocalDateTime collectionTime;
}