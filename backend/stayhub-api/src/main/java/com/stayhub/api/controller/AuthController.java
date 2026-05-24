package com.stayhub.api.controller;

import com.stayhub.api.dto.*;
import com.stayhub.api.service.StayhubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private StayhubService stayhubService;

    // 1. API ĐĂNG KÝ CHUNG CHO 1 ỨNG DỤNG (Phân luồng tự động dựa trên appType)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        try {
            String result = stayhubService.registerUser(
                    request.getEmail(),
                    request.getPassword(),
                    request.getPhoneNumber(),
                    request.getAppType() != null ? request.getAppType() : "OWNER_APP" // Mặc định là chủ nhà nếu để trống
            );

            Map<String, Object> response = new HashMap<>();
            response.put("status", "Success");
            response.put("message", result);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Đăng ký thất bại: " + e.getMessage());
        }
    }

    // 2. API ĐĂNG NHẬP CHUNG TẬP TRUNG (ĐÃ ĐỒNG BỘ ĐỘNG USER ID CHO MOBILE)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 🌟 ĐỒNG BỘ: Gọi hàm login trả về Object AuthResponse trực tiếp từ Service
            AuthResponse authResponse = stayhubService.login(request.getEmail(), request.getPassword());

            // Trả về trực tiếp authResponse (đã chứa đủ 5 trường: id, token, email, role, plan)
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Đăng nhập thất bại: " + e.getMessage());
        }
    }
}