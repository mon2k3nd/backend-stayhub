package com.stayhub.api.controller;

import com.stayhub.api.entity.MaintenanceRequest;
import com.stayhub.api.service.StayhubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MaintenanceController {

    private final StayhubService stayhubService;

    // Tenant gửi báo cáo sự cố
    @PostMapping("/tenant/report")
    public ResponseEntity<?> tenantReportIssue(@RequestBody Map<String, Object> body) {
        try {
            Long tenantId = Long.valueOf(body.get("tenantId").toString());
            Long roomId = Long.valueOf(body.get("roomId").toString());
            String title = body.get("title") != null
                    ? body.get("title").toString()
                    : (body.get("category") != null ? body.get("category").toString() : "Sự cố");
            String description = body.get("description").toString();
            MaintenanceRequest result = stayhubService.tenantCreateRequest(tenantId, roomId, title, description);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi gửi báo cáo: " + e.getMessage());
        }
    }

    // FIX: Thêm endpoint GET /tenant/{tenantId} để Flutter app có thể lấy lịch sử sự cố
    // Trước đây endpoint này không tồn tại → app nhận 404/500 mỗi lần load
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<MaintenanceRequest>> getTenantMaintenanceHistory(
            @PathVariable Long tenantId) {
        return ResponseEntity.ok(stayhubService.getMaintenanceForTenant(tenantId));
    }

    // Owner xem danh sách sự cố (có filter)
    @GetMapping("/owner/list/{ownerId}")
    public ResponseEntity<List<MaintenanceRequest>> getOwnerMaintenanceList(
            @PathVariable Long ownerId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(stayhubService.getMaintenanceForOwner(ownerId, status, search));
    }

    // Owner phân công nhân viên xử lý sự cố
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

    // Nhân viên cập nhật trạng thái sự cố
    @PutMapping("/staff/updateStatus")
    public ResponseEntity<?> staffUpdateStatus(@RequestBody Map<String, Object> body) {
        try {
            Long requestId = Long.valueOf(body.get("requestId").toString());
            String status = body.get("status").toString();
            MaintenanceRequest updated = stayhubService.staffUpdateStatus(requestId, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật trạng thái: " + e.getMessage());
        }
    }

    // Owner xóa sự cố
    @DeleteMapping("/owner/delete/{requestId}")
    public ResponseEntity<?> ownerDeleteRequest(
            @PathVariable Long requestId,
            @RequestParam Long ownerId) {
        try {
            stayhubService.ownerDeleteMaintenance(requestId, ownerId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa yêu cầu thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xóa dữ liệu: " + e.getMessage());
        }
    }
}
