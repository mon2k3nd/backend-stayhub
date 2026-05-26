package com.stayhub.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stayhub.api.dto.response.ApiResponse;
import com.stayhub.api.entity.Room;
import com.stayhub.api.service.StayhubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final StayhubService stayhubService;

    @PostMapping(value = "/add/{ownerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Room>> addRoom(
            @PathVariable Long ownerId,
            @RequestPart("room") String roomJson,
            @RequestPart(value = "roomFiles", required = false) List<MultipartFile> roomFiles,
            @RequestPart(value = "inspectionFiles", required = false) List<MultipartFile> inspectionFiles
    ) {
        try {
            // 1. Chuyển đổi chuỗi JSON gửi từ Frontend thành Object Room Entity
            ObjectMapper objectMapper = new ObjectMapper();
            Room roomDetails = objectMapper.readValue(roomJson, Room.class);

            // 2. Logic lưu file cục bộ/cloud để sinh URL (Ở đây đang làm luồng Mock dữ liệu tên file)
            List<String> roomImageUrls = new ArrayList<>();
            if (roomFiles != null) {
                for (MultipartFile file : roomFiles) {
                    // Thực tế: fileService.upload(file) -> trả về URL từ Cloudinary/S3
                    roomImageUrls.add("/uploads/rooms/" + System.currentTimeMillis() + "_" + file.getOriginalFilename());
                }
            }

            List<String> inspectionImageUrls = new ArrayList<>();
            if (inspectionFiles != null) {
                for (MultipartFile file : inspectionFiles) {
                    inspectionImageUrls.add("/uploads/inspections/" + System.currentTimeMillis() + "_" + file.getOriginalFilename());
                }
            }

            // 3. Gọi dịch vụ xử lý lưu trữ Database
            Room savedRoom = stayhubService.createRoom(ownerId, roomDetails, roomImageUrls, inspectionImageUrls);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<Room>builder()
                            .success(true)
                            .message("Thêm phòng trọ SaaS kèm hình ảnh thành công!")
                            .data(savedRoom)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<Room>builder()
                            .success(false)
                            .message("Lỗi xử lý hệ thống: " + e.getMessage())
                            .build()
            );
        }
    }
}