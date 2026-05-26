package com.stayhub.api.controller;

import com.stayhub.api.dto.request.RoomRequestDTO;
import com.stayhub.api.dto.response.ApiResponse;
import com.stayhub.api.entity.Room;
import com.stayhub.api.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApiController {

    private final RoomService roomService;

    /**
     * Thêm phòng nhanh không kèm ảnh (JSON only)
     * POST /api/owner/rooms/{ownerId}
     */
    @PostMapping("/rooms/{ownerId}")
    public ResponseEntity<ApiResponse<Room>> addRoom(
            @PathVariable Long ownerId,
            @Valid @RequestBody RoomRequestDTO request) {
        try {
            Room newRoom = roomService.addRoom(ownerId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<Room>builder()
                            .success(true)
                            .message("Thêm phòng thành công!")
                            .data(newRoom)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Room>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }
}