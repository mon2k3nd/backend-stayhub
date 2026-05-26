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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomController {

    private final StayhubService stayhubService;
    private final ObjectMapper objectMapper;

    // Thư mục lưu ảnh — trong thực tế nên dùng biến môi trường
    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping
    public ResponseEntity<ApiResponse<List<Room>>> getAllRooms() {
        try {
            List<Room> rooms = stayhubService.getAllRooms();
            return ResponseEntity.ok(ApiResponse.<List<Room>>builder()
                    .success(true).message("Lấy danh sách phòng thành công!").data(rooms).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<Room>>builder().success(false).message("Lỗi: " + e.getMessage()).build());
        }
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<Room>>> getRoomsByOwner(@PathVariable Long ownerId) {
        try {
            List<Room> rooms = stayhubService.getRoomsByOwner(ownerId);
            return ResponseEntity.ok(ApiResponse.<List<Room>>builder()
                    .success(true).message("Thành công!").data(rooms).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<Room>>builder().success(false).message("Lỗi: " + e.getMessage()).build());
        }
    }

    @PostMapping(value = "/add/{ownerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Room>> addRoom(
            @PathVariable Long ownerId,
            @RequestPart("room") String roomJson,
            @RequestPart(value = "roomFiles", required = false) List<MultipartFile> roomFiles,
            @RequestPart(value = "inspectionFiles", required = false) List<MultipartFile> inspectionFiles
    ) {
        try {
            Room roomDetails = objectMapper.readValue(roomJson, Room.class);

            // FIX: Lưu file ảnh thực sự vào disk
            List<String> roomImageUrls = saveFiles(roomFiles, "rooms");
            List<String> inspectionImageUrls = saveFiles(inspectionFiles, "inspections");

            Room savedRoom = stayhubService.createRoom(ownerId, roomDetails, roomImageUrls, inspectionImageUrls);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<Room>builder().success(true).message("Thêm phòng thành công!").data(savedRoom).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<Room>builder().success(false).message("Lỗi: " + e.getMessage()).build());
        }
    }

    @PutMapping("/{roomId}/owner/{ownerId}")
    public ResponseEntity<ApiResponse<Room>> updateRoom(
            @PathVariable Long roomId,
            @PathVariable Long ownerId,
            @RequestBody Room updatedData) {
        try {
            Room updated = stayhubService.updateRoom(roomId, ownerId, updatedData);
            return ResponseEntity.ok(ApiResponse.<Room>builder()
                    .success(true).message("Cập nhật phòng thành công!").data(updated).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<Room>builder().success(false).message(e.getMessage()).build());
        }
    }

    @DeleteMapping("/{roomId}/owner/{ownerId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(
            @PathVariable Long roomId,
            @PathVariable Long ownerId) {
        try {
            stayhubService.deleteRoom(roomId, ownerId);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true).message("Xóa phòng thành công!").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<Void>builder().success(false).message(e.getMessage()).build());
        }
    }

    private List<String> saveFiles(List<MultipartFile> files, String subDir) throws IOException {
        List<String> urls = new ArrayList<>();
        if (files == null || files.isEmpty()) return urls;

        Path dirPath = Paths.get(UPLOAD_DIR + subDir);
        Files.createDirectories(dirPath);

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = dirPath.resolve(filename);
            Files.write(filePath, file.getBytes());
            urls.add("/uploads/" + subDir + "/" + filename);
        }
        return urls;
    }
}