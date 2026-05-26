package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bills")
@Data
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;
    private Integer month;
    private Integer year;
    private String electricImage;
    private String waterImage;
    private Double totalAmount;
    private Boolean isPaid;
}