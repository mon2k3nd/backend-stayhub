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

    // FIX: Constructor injection thay @Autowired field injection
    private final StayhubService stayhubService;

    @PostMapping("/tenant/report")
    public ResponseEntity<?> tenantReportIssue(@RequestBody Map<String, Object> body) {
        try {
            Long tenantId = Long.valueOf(body.get("tenantId").toString());
            Long roomId = Long.valueOf(body.get("roomId").toString());
            String title = body.get("title") != null
                    ? body.get("title").toString()
                    : body.get("category").toString();
            String description = body.get("description").toString();

            // FIX: Goi dung tenantCreateRequest (da them vao StayhubService)
            MaintenanceRequest result = stayhubService.tenantCreateRequest(tenantId, roomId, title, description);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Loi gui bao cao: " + e.getMessage());
        }
    }

    @GetMapping("/owner/list/{ownerId}")
    public ResponseEntity<List<MaintenanceRequest>> getOwnerMaintenanceList(
            @PathVariable Long ownerId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search) {
        // FIX: Goi dung getMaintenanceForOwner (da them vao StayhubService)
        return ResponseEntity.ok(stayhubService.getMaintenanceForOwner(ownerId, status, search));
    }

    @PutMapping("/owner/assign")
    public ResponseEntity<?> ownerAssignStaff(@RequestBody Map<String, Long> body) {
        try {
            Long requestId = body.get("requestId");
            Long staffId = body.get("staffId");
            return ResponseEntity.ok(stayhubService.ownerAssignStaff(requestId, staffId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Loi phan viec: " + e.getMessage());
        }
    }

    @PutMapping("/staff/updateStatus")
    public ResponseEntity<?> staffUpdateStatus(@RequestBody Map<String, Object> body) {
        try {
            Long requestId = Long.valueOf(body.get("requestId").toString());
            String status = body.get("status").toString();
            MaintenanceRequest updated = stayhubService.staffUpdateStatus(requestId, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Loi cap nhat trang thai: " + e.getMessage());
        }
    }

    @DeleteMapping("/owner/delete/{requestId}")
    public ResponseEntity<?> ownerDeleteRequest(
            @PathVariable Long requestId,
            @RequestParam Long ownerId) {
        try {
            stayhubService.ownerDeleteMaintenance(requestId, ownerId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Da xoa yeu cau thanh cong!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Loi xoa du lieu: " + e.getMessage());
        }
    }
}