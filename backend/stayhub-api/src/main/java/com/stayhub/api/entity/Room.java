package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Column(name = "price")
    private Double price;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoomStatus status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "deposit")
    private Double deposit;

    @Column(name = "max_guests")
    private Integer maxGuests;

    @Column(name = "electricity_price")
    private Double electricityPrice;

    @Column(name = "water_price")
    private Double waterPrice;

    @Column(name = "service_fee")
    private Double serviceFee;

    @Column(name = "room_images", columnDefinition = "TEXT")
    private String roomImages;

    @Column(name = "inspection_images", columnDefinition = "TEXT")
    private String inspectionImages;
}