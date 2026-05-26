package com.stayhub.api.controller;

import com.stayhub.api.entity.Bill;
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

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Bill>> getRoomBills(@PathVariable Long roomId) {
        return ResponseEntity.ok(stayhubService.getAllBills(roomId));
    }

    @PostMapping
    public ResponseEntity<?> createBill(@RequestBody Bill bill) {
        try {
            if (bill.getRoomId() == null) {
                return ResponseEntity.badRequest().body("Trường 'roomId' không được để trống!");
            }
            if (bill.getMonth() == null || bill.getMonth() < 1 || bill.getMonth() > 12) {
                return ResponseEntity.badRequest().body("Tháng không hợp lệ (1-12)!");
            }
            if (bill.getYear() == null || bill.getYear() < 2020) {
                return ResponseEntity.badRequest().body("Năm không hợp lệ!");
            }
            if (bill.getTotalAmount() == null || bill.getTotalAmount() < 0) {
                return ResponseEntity.badRequest().body("Tổng tiền không hợp lệ!");
            }

            // FIX: Dùng EXISTS query trực tiếp thay vì tải toàn bộ bills rồi filter
            // Trước đây: getAllBills(roomId).stream().anyMatch(...) → tải tất cả bills
            // Bây giờ: SELECT EXISTS(...) → 1 query nhanh, không tải data thừa
            if (stayhubService.billExistsForMonth(bill.getRoomId(), bill.getMonth(), bill.getYear())) {
                return ResponseEntity.badRequest()
                        .body("Hóa đơn tháng " + bill.getMonth() + "/" + bill.getYear()
                                + " cho phòng này đã tồn tại!");
            }

            if (bill.getIsPaid() == null) {
                bill.setIsPaid(false);
            }
            Bill created = stayhubService.createBill(bill);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo hóa đơn: " + e.getMessage());
        }
    }

    @PutMapping("/update-status/{billId}")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long billId,
            @RequestBody Map<String, Boolean> body
    ) {
        try {
            Boolean isPaid = body.get("isPaid");
            if (isPaid == null) {
                return ResponseEntity.badRequest().body("Trường 'isPaid' không được để trống!");
            }
            Bill updatedBill = stayhubService.updateBillStatus(billId, isPaid);
            return ResponseEntity.ok(updatedBill);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật hóa đơn: " + e.getMessage());
        }
    }
}
