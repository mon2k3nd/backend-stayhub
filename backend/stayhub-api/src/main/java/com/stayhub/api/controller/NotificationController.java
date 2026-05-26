package com.stayhub.api.controller;

import com.stayhub.api.dto.response.ApiResponse;
import com.stayhub.api.entity.Notification;
import com.stayhub.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.<List<Notification>>builder()
                .success(true).message("Thành công!")
                .data(notificationService.getByUser(userId)).build());
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .success(true).message("Thành công!")
                .data(notificationService.countUnread(userId)).build());
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Đã đọc!").build());
    }

    @PostMapping("/user/{userId}/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(@PathVariable Long userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Đã đọc tất cả!").build());
    }
}
