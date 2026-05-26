package com.stayhub.api.controller;

import com.stayhub.api.dto.request.LoginRequest;
import com.stayhub.api.dto.request.RegisterRequest;
import com.stayhub.api.dto.response.ApiResponse;
import com.stayhub.api.dto.response.LoginResponse;
import com.stayhub.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    /** Đăng ký tài khoản mới (mặc định role = TENANT) */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            LoginResponse response = userService.register(request);
            return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                    .success(true)
                    .message("Đăng ký thành công!")
                    .data(response)
                    .build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<LoginResponse>builder()
                            .success(false).message(e.getMessage()).build());
        }
    }

    /** Đăng nhập bằng số điện thoại + mật khẩu */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                    .success(true)
                    .message("Đăng nhập thành công!")
                    .data(response)
                    .build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<LoginResponse>builder()
                            .success(false).message(e.getMessage()).build());
        }
    }

    /** Cập nhật FCM token (gọi sau khi đăng nhập thành công) */
    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String fcmToken = (String) body.get("fcmToken");
            userService.updateFcmToken(userId, fcmToken);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true).message("Cập nhật FCM token thành công!").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder().success(false).message(e.getMessage()).build());
        }
    }

    /** Lấy thông tin profile của user đang đăng nhập */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<Object>> getProfile(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true).message("Thành công!")
                    .data(userService.getById(userId)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.builder().success(false).message(e.getMessage()).build());
        }
    }
}
