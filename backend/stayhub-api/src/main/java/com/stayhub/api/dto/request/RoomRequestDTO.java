package com.stayhub.api.dto.request;

import com.stayhub.api.entity.RoomStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RoomRequestDTO {

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @Positive(message = "Price must be greater than 0")
    private Double price;

    private RoomStatus status = RoomStatus.TRONG;

    private Double deposit = 0.0;

    private Double electricityPrice = 4000.0;

    private Double waterPrice = 30000.0;

    private Double serviceFee = 50000.0;

    // Bổ sung các trường đồng bộ cho App Mobile
    private String title;
    private String description;
    private String location;
    private Integer maxGuests;
    private String buildingCode;
}