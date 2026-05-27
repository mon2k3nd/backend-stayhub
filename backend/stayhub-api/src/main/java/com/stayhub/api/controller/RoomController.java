package com.stayhub.api.controller;

import com.stayhub.api.dto.response.ApiResponse;
import com.stayhub.api.entity.Room;
import com.stayhub.api.entity.RoomStatus;
import com.stayhub.api.exception.ResourceNotFoundException;
import com.stayhub.api.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;

    /** Lấy danh sách phòng theo chi nhánh */
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<Room>>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.<List<Room>>builder()
                .success(true).message("Thành công!")
                .data(roomRepository.findByBranchId(branchId))
                .build());
    }

    /** Lấy danh sách tất cả phòng của chủ nhà (có phân trang) */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<Room>>> getByOwner(
            @PathVariable Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String status) {

        if (status != null) {
            RoomStatus roomStatus = RoomStatus.valueOf(status);
            return ResponseEntity.ok(ApiResponse.<List<Room>>builder()
                    .success(true).message("Thành công!")
                    .data(roomRepository.findByOwnerIdAndStatus(ownerId, roomStatus))
                    .build());
        }

        Page<Room> roomPage = roomRepository.findByOwnerIdPaged(ownerId,
                PageRequest.of(page, size, Sort.by("roomName").ascending()));
        return ResponseEntity.ok(ApiResponse.<List<Room>>builder()
                .success(true).message("Thành công!")
                .data(roomPage.getContent())
                .build());
    }

    /** Lấy thông tin một phòng */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Room>> getById(@PathVariable Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ID: " + id));
        return ResponseEntity.ok(ApiResponse.<Room>builder()
                .success(true).message("Thành công!")
                .data(room)
                .build());
    }

    /** Tạo phòng mới — chỉ chủ nhà mới được tạo */
    @PostMapping
    public ResponseEntity<ApiResponse<Room>> create(@RequestBody Room room, Authentication auth) {
        room.setOwnerId((Long) auth.getPrincipal());
        Room saved = roomRepository.save(room);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Room>builder()
                        .success(true).message("Tạo phòng thành công!")
                        .data(saved)
                        .build());
    }

    /** Cập nhật thông tin phòng */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Room>> update(
            @PathVariable Long id,
            @RequestBody Room updated,
            Authentication auth) {

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ID: " + id));

        if (!room.getOwnerId().equals((Long) auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Room>builder()
                            .success(false).message("Bạn không có quyền chỉnh sửa phòng này!")
                            .build());
        }

        updated.setId(id);
        updated.setOwnerId(room.getOwnerId());
        return ResponseEntity.ok(ApiResponse.<Room>builder()
                .success(true).message("Cập nhật phòng thành công!")
                .data(roomRepository.save(updated))
                .build());
    }

    /** Xóa phòng */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Authentication auth) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ID: " + id));

        if (!room.getOwnerId().equals((Long) auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Void>builder()
                            .success(false).message("Bạn không có quyền xóa phòng này!")
                            .build());
        }

        if (room.getStatus() == RoomStatus.DA_THUE) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<Void>builder()
                            .success(false).message("Không thể xóa phòng đang có khách thuê!")
                            .build());
        }

        roomRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Đã xóa phòng!").build());
    }
}
