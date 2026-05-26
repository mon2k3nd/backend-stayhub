package com.stayhub.api.service.impl;

import com.stayhub.api.dto.request.RoomRequestDTO;
import com.stayhub.api.entity.Room;
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

        // SỬA: Đọc chuẩn dữ liệu từ RoomRequestDTO và set vào các trường thực tế của Room.java
        String roomName = (request.getRoomNumber() != null) ? request.getRoomNumber() : "";
        room.setRoomName(roomName);

        Double roomPrice = (request.getPrice() != null) ? request.getPrice() : 0.0;
        room.setPrice(roomPrice);

        String address = (request.getLocation() != null) ? request.getLocation() : "";
        room.setAddress(address);

        // Khởi tạo chuỗi văn bản rỗng cho ảnh để tương thích kiểu String trong Room.java
        room.setRoomImages("");
        room.setInspectionImages("");

        return roomRepo.save(room);
    }
}