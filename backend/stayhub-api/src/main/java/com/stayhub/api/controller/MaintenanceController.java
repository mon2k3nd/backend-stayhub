package com.stayhub.api.controller;

import com.stayhub.api.entity.MaintenanceRequest;
import com.stayhub.api.service.StayhubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins = "*")
public class MaintenanceController {

    @Autowired
    private StayhubService stayhubService;

    // 🟢 [KHÁCH THUÊ] Gửi báo hỏng thiết bị siêu tốc bằng Bottom Sheet từ App Flutter
    @PostMapping("/tenant/report")
    public ResponseEntity<?> tenantReportIssue(@RequestBody Map<String, Object> body) {
        try {
            Long tenantId = Long.valueOf(body.get("tenantId").toString());
            Long roomId = Long.valueOf(body.get("roomId").toString());

            // Chấp nhận cả key "title" hoặc "category" gửi lên từ mobile để tránh crash
            String title = body.get("title") != null ? body.get("title").toString() : body.get("category").toString();
            String description = body.get("description").toString();

            MaintenanceRequest result = stayhubService.tenantCreateRequest(tenantId, roomId, title, description);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi gửi báo cáo: " + e.getMessage());
        }
    }

    // 🔵 [CHỦ NHÀ] Lấy danh sách kết hợp TÌM KIẾM + PHÂN LOẠI TAB TRẠNG THÁI + BỘ LỌC
    @GetMapping("/owner/list/{ownerId}")
    public ResponseEntity<List<MaintenanceRequest>> getOwnerMaintenanceList(
            @PathVariable Long ownerId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(stayhubService.getMaintenanceForOwner(ownerId, status, search));
    }

    // 🔵 [CHỦ NHÀ] Giao việc cho Nhân viên kỹ thuật hiện trường
    @PutMapping("/owner/assign")
    public ResponseEntity<?> ownerAssignStaff(@RequestBody Map<String, Long> body) {
        try {
            Long requestId = body.get("requestId");
            Long staffId = body.get("staffId");
            return ResponseEntity.ok(stayhubService.ownerAssignStaff(requestId, staffId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi phân việc: " + e.getMessage());
        }
    }

    // 🟠 [NHÂN VIÊN] Cập nhật trạng thái sửa chữa (Fix triệt để lỗi "cannot be applied to given types")
    @PutMapping("/staff/updateStatus")
    public ResponseEntity<?> staffUpdateStatus(@RequestBody Map<String, Object> body) {
        try {
            Long requestId = Long.valueOf(body.get("requestId").toString());
            String status = body.get("status").toString();

            // 🌟 CHUẨN HÓA: Chỉ truyền đúng 2 tham số (requestId, status) khớp 100% với StayhubService đã sửa
            MaintenanceRequest updated = stayhubService.staffUpdateStatus(requestId, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật trạng thái: " + e.getMessage());
        }
    }

    // 🔴 [CHỦ NHÀ] Xóa sự cố rác hoặc yêu cầu trùng lặp
    @DeleteMapping("/owner/delete/{requestId}")
    public ResponseEntity<?> ownerDeleteRequest(@PathVariable Long requestId, @RequestParam Long ownerId) {
        try {
            stayhubService.ownerDeleteMaintenance(requestId, ownerId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa yêu cầu thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xóa dữ liệu: " + e.getMessage());
        }
    }
}