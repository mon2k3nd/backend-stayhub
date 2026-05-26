package com.stayhub.api.service.impl;

import com.stayhub.api.dto.request.RoomRequestDTO;
import com.stayhub.api.entity.Room;
import com.stayhub.api.entity.RoomStatus;
import com.stayhub.api.repository.RoomRepository;
import com.stayhub.api.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepo;

    @Override
    public Room addRoom(Long ownerId, RoomRequestDTO request) {
        Room room = new Room();
        room.setOwnerId(ownerId);

        room.setRoomName(request.getRoomNumber() != null ? request.getRoomNumber() : "");
        room.setPrice(request.getPrice() != null ? request.getPrice() : 0.0);
        room.setAddress(request.getLocation() != null ? request.getLocation() : "");

        // status enum → enum, gán thẳng
        room.setStatus(request.getStatus() != null ? request.getStatus() : RoomStatus.TRONG);

        room.setDescription(request.getDescription());
        room.setDeposit(request.getDeposit() != null ? request.getDeposit() : 0.0);
        room.setMaxGuests(request.getMaxGuests());
        room.setElectricityPrice(request.getElectricityPrice() != null ? request.getElectricityPrice() : 4000.0);
        room.setWaterPrice(request.getWaterPrice() != null ? request.getWaterPrice() : 30000.0);
        room.setServiceFee(request.getServiceFee() != null ? request.getServiceFee() : 50000.0);

        room.setRoomImages("");
        room.setInspectionImages("");

        return roomRepo.save(room);
    }
}