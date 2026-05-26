package com.stayhub.api.controller;

import com.stayhub.api.dto.AuthResponse;
import com.stayhub.api.dto.LoginRequest;
import com.stayhub.api.service.StayhubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private StayhubService stayhubService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("EMAIL: " + request.getEmail());
            System.out.println("PHONE: " + request.getPhoneNumber());

            String identifier = null;
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
                identifier = request.getPhoneNumber();
            } else if (request.getEmail() != null && !request.getEmail().isBlank()) {
                identifier = request.getEmail();
            }

            if (identifier == null) {
                return ResponseEntity.badRequest().body("Thiếu email hoặc số điện thoại!");
            }

            AuthResponse response = stayhubService.login(identifier, request.getPassword());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 🟢 CẬP NHẬT LOGIC ĐĂNG KÝ THỰC TẾ LƯU VÀO DATABASE PHÍA DƯỚI:
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> registerData) {
        try {
            // 1. Đọc dữ liệu JSON do ứng dụng Flutter gửi lên thông qua Body Request
            String name = (String) registerData.get("name");
            String phoneNumber = (String) registerData.get("phoneNumber");
            String password = (String) registerData.get("password");
            String email = (String) registerData.get("email");
            String appType = (String) registerData.get("appType");

            // 2. Kiểm tra tính hợp lệ cơ bản của dữ liệu đầu vào
            if (phoneNumber == null || phoneNumber.isBlank() || password == null || password.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Số điện thoại và mật khẩu không được để trống!"
                ));
            }

            // 3. Gọi hàm xử lý cốt lõi từ StayhubService để kiểm tra trùng lặp, mã hóa mật khẩu và lưu vào DB
            String serviceMessage = stayhubService.registerUser(name, password, phoneNumber, email, appType);

            // 4. Trả phản hồi thành công (201 Created) về cho Flutter
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "success",
                    "message", serviceMessage
            ));

        } catch (RuntimeException e) {
            // Bắt các lỗi nghiệp vụ từ Service ném ra (Ví dụ: Số điện thoại đã tồn tại, Khách thuê chưa được khai báo,...)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Lỗi hệ thống nội bộ: " + e.getMessage()
            ));
        }
    }
}