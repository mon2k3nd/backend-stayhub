package com.stayhub.api.service;

import com.stayhub.api.dto.request.RoomRequestDTO;
import com.stayhub.api.entity.Room;

public interface RoomService {
    Room addRoom(Long ownerId, RoomRequestDTO request);
}