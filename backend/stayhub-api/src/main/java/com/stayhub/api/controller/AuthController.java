package com.stayhub.api.controller;

import com.stayhub.api.dto.request.LoginRequest;
import com.stayhub.api.dto.request.RegisterRequest;
import com.stayhub.api.dto.response.ApiResponse;
import com.stayhub.api.dto.response.LoginResponse;
import com.stayhub.api.dto.response.UserProfileResponse;
import com.stayhub.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /** Đăng ký tài khoản mới (mặc định role = TENANT) */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = userService.register(request);
        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Đăng ký thành công!")
                .data(response)
                .build());
    }

    /** Đăng nhập bằng số điện thoại + mật khẩu */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Đăng nhập thành công!")
                .data(response)
                .build());
    }

    /**
     * Cập nhật FCM token — lấy userId từ JWT, không tin pathparam.
     */
    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            Authentication auth,
            @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        String fcmToken = body.get("fcmToken");
        userService.updateFcmToken(userId, fcmToken);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Cập nhật FCM token thành công!").build());
    }

    /**
     * Lấy thông tin profile của chính user đang đăng nhập.
     * userId lấy từ JWT — không cho phép xem profile người khác.
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        UserProfileResponse profile = userService.getProfileById(userId);
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("Thành công!")
                .data(profile)
                .build());
    }

    /**
     * Cập nhật thông tin cá nhân.
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            Authentication auth,
            @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        UserProfileResponse updated = userService.updateProfile(userId, body);
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("Cập nhật thành công!")
                .data(updated)
                .build());
    }

    /**
     * Đổi mật khẩu.
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication auth,
            @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        userService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Đổi mật khẩu thành công!").build());
    }
}
