package com.stayhub.api.controller;

import com.stayhub.api.entity.Room;
import com.stayhub.api.dto.request.RoomRequestDTO;
import com.stayhub.api.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final RoomService roomService;

    @PostMapping("/rooms/owner/{ownerId}")
    public ResponseEntity<?> addRoom(@PathVariable Long ownerId, @RequestBody RoomRequestDTO request) {
        try {
            Room newRoom = roomService.addRoom(ownerId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(newRoom);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Thêm phòng trọ thất bại: " + e.getMessage());
        }
    }
}