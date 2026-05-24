package com.stayhub.api.controller;

import com.stayhub.api.entity.*;
import com.stayhub.api.service.StayhubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
// 🌟 SỬA LỖI TẠI ĐÂY: Loại bỏ hoàn toàn @CrossOrigin(origins = "*") để tránh xung đột cấu hình
// AllowCredentials(true) của Spring Security gây ra lỗi chặn 403 bừa bãi trên Mobile.
public class ApiController {

    private final StayhubService service;
    private static final String UP_DIR = "uploads/";

    // 1. Cổng upload ảnh công tơ / ảnh CCCD
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile f) throws IOException {
        Path p = Paths.get(UP_DIR);
        if (!Files.exists(p)) Files.createDirectories(p);
        String name = System.currentTimeMillis() + "_" + f.getOriginalFilename();
        Files.copy(f.getInputStream(), p.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("/uploads/" + name);
    }

    // 🌟 ĐỒNG BỘ: API THÊM PHÒNG TRỌ MỚI CHO CHỦ NHÀ
    // Khớp chính xác với cấu hình .requestMatchers("/api/owner/**").hasAnyRole("ADMIN", "OWNER")
    @PostMapping("/owner/rooms/add/{ownerId}")
    public ResponseEntity<?> addRoom(@PathVariable Long ownerId, @RequestBody Room roomData) {
        try {
            Room savedRoom = service.ownerAddRoom(ownerId, roomData);
            return new ResponseEntity<>(savedRoom, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Thêm phòng trọ thất bại: " + e.getMessage());
        }
    }

    // 2. CHỦ NHÀ TẠO TÀI KHOẢN CHO NHÂN VIÊN (STAFF)
    @PostMapping("/owner/create-staff")
    public ResponseEntity<String> createStaff(@RequestParam Long ownerId, @RequestBody User staffData) {
        try {
            String result = service.ownerCreateStaff(
                    staffData.getEmail(),
                    staffData.getPassword(),
                    staffData.getPhoneNumber(),
                    ownerId
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Tạo nhân viên thất bại: " + e.getMessage());
        }
    }

    // 3. NHÂN VIÊN/CHỦ NHÀ ĐĂNG KÝ HỒ SƠ KHÁCH THUÊ
    @PostMapping("/staff/tenant-profile")
    public ResponseEntity<?> createTenant(@RequestParam Long ownerId, @RequestParam Long roomId, @RequestBody TenantProfile profile) {
        try {
            TenantProfile savedProfile = service.ownerRegisterTenant(ownerId, roomId, profile);
            return ResponseEntity.ok(savedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Đăng ký hồ sơ thất bại: " + e.getMessage());
        }
    }

    // 4. NHÂN VIÊN GHI CHỈ SỐ ĐIỆN NƯỚC & TỰ ĐỘNG SINH HÓA ĐƠN
    @PostMapping("/staff/submit-usage")
    public ResponseEntity<Bill> submitUsage(
            @RequestParam Long roomId,
            @RequestParam Integer oldElectricity,
            @RequestParam Integer newElectricity,
            @RequestParam String electricityProofUrl,
            @RequestParam Integer oldWater,
            @RequestParam Integer newWater,
            @RequestParam String waterProofUrl,
            @RequestParam Long ownerId) {
        try {
            Bill generatedBill = service.staffInputBill(
                    roomId, oldElectricity, newElectricity, electricityProofUrl,
                    oldWater, newWater, waterProofUrl, ownerId
            );
            return ResponseEntity.ok(generatedBill);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // 5. NHÂN VIÊN THU TIỀN MẶT & CHỦ NHÀ ĐỐI SOÁT
    @PostMapping("/staff/collect-cash")
    public ResponseEntity<?> collectCash(@RequestParam Long staffId, @RequestParam Long roomId,
                                         @RequestParam double amount, @RequestParam Long ownerId) {
        try {
            CashCollection cashCollection = service.staffCollectCash(staffId, roomId, amount, ownerId);
            return ResponseEntity.ok(cashCollection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ghi nhận thu tiền mặt thất bại: " + e.getMessage());
        }
    }

    // 6. CHỦ NHÀ XÁC NHẬN ĐỐI SOÁT BÀN GIAO TIỀN MẶT TỪ NHÂN VIÊN
    @PutMapping("/cash/handover")
    public ResponseEntity<String> confirmHandover(@RequestParam Long staffId) {
        try {
            service.ownerConfirmHandover(staffId);
            return ResponseEntity.ok("Chủ nhà đã đối soát xong và nhận đủ tiền mặt từ nhân viên!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Xác nhận bàn giao thất bại: " + e.getMessage());
        }
    }

    // 7. PHÂN TÍCH DỮ LIỆU BÁO CÁO NÂNG CAO (DÀNH RIÊNG CHO GÓI VIP)
    @GetMapping("/dashboards/vip-demographics")
    public ResponseEntity<?> getVipData(@RequestParam Long ownerId) {
        try {
            return ResponseEntity.ok(service.getVIPDemographicsDashboard(ownerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Lỗi truy cập dữ liệu: " + e.getMessage());
        }
    }

    // 8. API LẤY DANH SÁCH TẤT CẢ PHÒNG TRỌ (DÙNG CHUNG CHO MOBILE)
    @GetMapping("/rooms")
    public ResponseEntity<?> getAllRooms() {
        try {
            return ResponseEntity.ok(service.getAllRooms());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi lấy danh sách phòng trọ: " + e.getMessage());
        }
    }
}