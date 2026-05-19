package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "bills") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Bill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;
    private Long ownerId;
    private LocalDate billingMonth;

    private Integer oldElectricityNumber;
    private Integer newElectricityNumber;
    private String electricityMeterImageUrl; // Link ảnh công tơ điện

    private Integer oldWaterNumber;
    private Integer newWaterNumber;
    private String waterMeterImageUrl; // Link ảnh công tơ nước

    private Double baseRoomPrice;
    private Double penaltyFee; // Tiền phạt quá hạn tích lũy
    private Double totalAmount;

    private boolean isApprovedByOwner; // Chủ nhà bấm chốt thì khách mới xem được QR
    private boolean isPaid;

    @Column(length = 1000)
    private String qrCodeUrl;
}