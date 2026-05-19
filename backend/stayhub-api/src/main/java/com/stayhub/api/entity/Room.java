package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "rooms") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String buildingCode; // Ví dụ: LM (Landmark)
    private String roomNumber;   // Ví dụ: 302
    private Double price;
    private Double deposit;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    private String contractImageUrl;
    private String inspectionImageUrl;

    // Đơn giá cấu hình linh hoạt theo từng phòng
    private Double electricityPrice;
    private Double waterPrice;
    private Double serviceFee; // Các loại phí dịch vụ cố định cộng thêm (mạng, vệ sinh...)

    private Long ownerId; // Multi-tenant Filter
}