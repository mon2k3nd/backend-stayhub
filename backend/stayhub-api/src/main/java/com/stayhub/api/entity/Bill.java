package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;
    private Long ownerId;
    private Integer month; // Sửa lỗi getMonth() và builder .month()
    private Integer year;

    private Integer oldElectricity;
    private Integer newElectricity;
    private String electricityProofUrl;

    private Integer oldWater;
    private Integer newWater;
    private String waterProofUrl;

    private Double totalAmount;
    private Boolean isPaid; // Sửa lỗi trạng thái hóa đơn
}