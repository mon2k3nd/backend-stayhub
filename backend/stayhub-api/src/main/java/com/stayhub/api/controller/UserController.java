package com.stayhub.api.controller;

import com.stayhub.api.entity.User;
import com.stayhub.api.service.StayhubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final StayhubService stayhubService;

    /**
     * Cập nhật thông tin hồ sơ cá nhân
     * PUT /api/users/{userId}/profile
     */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body
    ) {
        try {
            User updated = stayhubService.updateUserProfile(
                    userId,
                    body.get("name"),
                    body.get("email"),
                    body.get("cccdNumber"),
                    body.get("hometown"),
                    body.get("gender")
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật hồ sơ thành công!",
                    "name", updated.getName() != null ? updated.getName() : "",
                    "email", updated.getEmail() != null ? updated.getEmail() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Đổi mật khẩu
     * PUT /api/users/{userId}/change-password
     */
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body
    ) {
        try {
            stayhubService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of("success", true, "message", "Đổi mật khẩu thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}