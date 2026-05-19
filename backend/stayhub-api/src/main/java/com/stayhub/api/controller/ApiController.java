package com.stayhub.api.controller;

import com.stayhub.api.entity.*;
import com.stayhub.api.service.StayhubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Mở khóa CORS kết nối đồng thời với cả Flutter, React Native và Web
public class ApiController {
    private final StayhubService service;
    private static final String UP_DIR = "uploads/";

    // Cổng đăng ký đăng nhập dùng chung
    @PostMapping("/auth/register") public ResponseEntity<String> reg(@RequestBody User u) { return ResponseEntity.ok(service.registerOwner(u)); }
    @PostMapping("/auth/login") public ResponseEntity<?> login(@RequestBody Map<String, String> r) { return ResponseEntity.ok(service.login(r.get("email"), r.get("password"))); }

    // Cổng upload ảnh công tơ / ảnh CCCD / ảnh nghiệm thu dùng chung cho các app khách thuê, nhân viên, chủ nhà
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile f) throws IOException {
        Path p = Paths.get(UP_DIR); if (!Files.exists(p)) Files.createDirectories(p);
        String name = System.currentTimeMillis() + "_" + f.getOriginalFilename();
        Files.copy(f.getInputStream(), p.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("/uploads/" + name);
    }

    // Cổng thao tác phòng và định danh nhân khẩu
    @PostMapping("/rooms/add") public ResponseEntity<?> addRoom(@RequestBody Room r, @RequestParam Long ownerId) { return ResponseEntity.ok(service.addRoom(r, ownerId)); }
    @PostMapping("/tenants/register") public ResponseEntity<?> regTenant(@RequestBody TenantProfile p, @RequestParam Long roomId, @RequestParam Long ownerId) { return ResponseEntity.ok(service.registerTenantProfile(p, roomId, ownerId)); }

    // Cổng nhập số điện nước và duyệt hóa đơn sinh mã VietQR
    @PostMapping("/bills/input")
    public ResponseEntity<?> inputIndices(@RequestParam Long roomId, @RequestParam Integer oldE, @RequestParam Integer newE, @RequestParam String eImg, @RequestParam Integer oldW, @RequestParam Integer newW, @RequestParam String wImg, @RequestParam Long ownerId) {
        return ResponseEntity.ok(service.inputMeterIndices(roomId, oldE, newE, eImg, oldW, newW, wImg, ownerId));
    }
    @PutMapping("/bills/{id}/approve") public ResponseEntity<?> approveBill(@PathVariable Long id) { return ResponseEntity.ok(service.approveAndGenerateVietQR(id)); }

    // Cổng quản lý tiền mặt cho nhân viên và chủ nhà đối soát
    @PostMapping("/cash/collect") public ResponseEntity<?> collectCash(@RequestParam Long staffId, @RequestParam Long roomId, @RequestParam Double amount, @RequestParam Long ownerId) {
        return ResponseEntity.ok(service.staffCollectCash(staffId, roomId, amount, ownerId));
    }
    @PutMapping("/cash/handover") public ResponseEntity<String> confirmHandover(@RequestParam Long staffId) { service.ownerConfirmHandover(staffId); return ResponseEntity.ok("Chủ nhà đã đối soát xong và nhận đủ tiền mặt!"); }

    // Cổng phân tích dữ liệu chuyên sâu (Chỉ chạy được với gói VIP)
    @GetMapping("/dashboards/vip-demographics") public ResponseEntity<?> getVipData(@RequestParam Long ownerId) { return ResponseEntity.ok(service.getVIPDemographicsDashboard(ownerId)); }
}