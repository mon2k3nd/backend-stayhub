package com.stayhub.api.controller;

import com.stayhub.api.entity.Bill;
import com.stayhub.api.entity.MaintenanceRequest;
import com.stayhub.api.service.StayhubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "*")
public class BillController {

    @Autowired
    private StayhubService stayhubService;

    // 🟢 SỬA LỖI TYPO: Đổi @getMapping thành @GetMapping chuẩn Spring Boot
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Bill>> getRoomBills(@PathVariable Long roomId) {
        return ResponseEntity.ok(stayhubService.getAllBills(roomId));
    }

    // 🟢 Cập nhật trạng thái duyệt thu tiền (Dành cho chức năng đối soát, cập nhật hóa đơn)
    @PutMapping("/update-status/{billId}")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long billId, @RequestBody Map<String, Boolean> body) {
        try {
            Boolean isPaid = body.get("isPaid");
            if (isPaid == null) {
                return ResponseEntity.badRequest().body("Trường 'isPaid' không được để trống!");
            }
            MaintenanceRequest updatedBill = stayhubService.staffUpdateStatus(billId, isPaid);
            return ResponseEntity.ok(updatedBill);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật hóa đơn: " + e.getMessage());
        }
    }
}