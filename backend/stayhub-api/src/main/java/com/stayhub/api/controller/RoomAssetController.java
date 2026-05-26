package com.stayhub.api.controller;

import com.stayhub.api.dto.response.ApiResponse;
import com.stayhub.api.entity.RoomAsset;
import com.stayhub.api.service.RoomAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomAssetController {

    private final RoomAssetService roomAssetService;

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<List<RoomAsset>>> getByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.<List<RoomAsset>>builder()
                .success(true).message("Thành công!")
                .data(roomAssetService.getByRoom(roomId)).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoomAsset>> create(@RequestBody RoomAsset asset) {
        try {
            return ResponseEntity.ok(ApiResponse.<RoomAsset>builder()
                    .success(true).message("Thêm tài sản thành công!")
                    .data(roomAssetService.create(asset)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<RoomAsset>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomAsset>> update(@PathVariable Long id,
                                                          @RequestBody RoomAsset asset) {
        try {
            return ResponseEntity.ok(ApiResponse.<RoomAsset>builder()
                    .success(true).message("Cập nhật tài sản thành công!")
                    .data(roomAssetService.update(id, asset)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<RoomAsset>builder().success(false).message(e.getMessage()).build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        roomAssetService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Đã xóa tài sản!").build());
    }
}
