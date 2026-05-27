package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "year", nullable = false)
    private Integer year;

    // Chỉ số điện cuối kỳ
    @Column(name = "electric_current")
    private Double electricCurrent;

    // Chỉ số điện đầu kỳ
    @Column(name = "electric_previous")
    private Double electricPrevious;

    // Chỉ số nước cuối kỳ
    @Column(name = "water_current")
    private Double waterCurrent;

    // Chỉ số nước đầu kỳ
    @Column(name = "water_previous")
    private Double waterPrevious;

    /** Đơn giá điện (kWh) — lấy từ hợp đồng */
    @Column(name = "electric_unit_price")
    private Double electricUnitPrice;

    /** Đơn giá nước (m3) — lấy từ hợp đồng */
    @Column(name = "water_unit_price")
    private Double waterUnitPrice;

    @Column(name = "electric_amount")
    private Double electricAmount;

    @Column(name = "water_amount")
    private Double waterAmount;

    @Column(name = "rent_amount")
    private Double rentAmount;

    @Column(name = "service_amount")
    private Double serviceAmount;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "electric_image")
    private String electricImage;

    @Column(name = "water_image")
    private String waterImage;

    // VietQR payment URL
    @Column(name = "payment_qr_url", columnDefinition = "TEXT")
    private String paymentQrUrl;

    @Column(name = "is_paid")
    @Builder.Default
    private Boolean isPaid = false;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "paid_by_cash")
    @Builder.Default
    private Boolean paidByCash = false;

    @Column(name = "collected_by_staff_id")
    private Long collectedByStaffId;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
