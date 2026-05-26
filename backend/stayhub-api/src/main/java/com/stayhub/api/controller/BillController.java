package com.stayhub.api.controller;

import com.stayhub.api.dto.response.ApiResponse;
import com.stayhub.api.entity.Bill;
import com.stayhub.api.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BillController {

    private final BillRepository billRepository;

    /** Lấy hóa đơn của khách thuê */
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<List<Bill>>> getByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(ApiResponse.<List<Bill>>builder()
                .success(true).message("Thành công!")
                .data(billRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)).build());
    }

    /** Lấy hóa đơn chưa thanh toán của khách */
    @GetMapping("/tenant/{tenantId}/unpaid")
    public ResponseEntity<ApiResponse<List<Bill>>> getUnpaid(@PathVariable Long tenantId) {
        return ResponseEntity.ok(ApiResponse.<List<Bill>>builder()
                .success(true).message("Thành công!")
                .data(billRepository.findUnpaidByTenant(tenantId)).build());
    }

    /** Chủ nhà / Nhân viên tạo hóa đơn tháng */
    @PostMapping
    public ResponseEntity<ApiResponse<Bill>> createBill(@RequestBody Bill bill) {
        try {
            // Tính tiền điện + nước
            if (bill.getElectricCurrent() != null && bill.getElectricPrevious() != null) {
                bill.setElectricAmount(
                        (bill.getElectricCurrent() - bill.getElectricPrevious()) * 3500.0);
            }
            if (bill.getWaterCurrent() != null && bill.getWaterPrevious() != null) {
                bill.setWaterAmount(
                        (bill.getWaterCurrent() - bill.getWaterPrevious()) * 15000.0);
            }

            double total = 0;
            if (bill.getRentAmount() != null) total += bill.getRentAmount();
            if (bill.getElectricAmount() != null) total += bill.getElectricAmount();
            if (bill.getWaterAmount() != null) total += bill.getWaterAmount();
            if (bill.getServiceAmount() != null) total += bill.getServiceAmount();
            bill.setTotalAmount(total);
            bill.setDueDate(LocalDateTime.now().plusDays(10));

            return ResponseEntity.ok(ApiResponse.<Bill>builder()
                    .success(true).message("Tạo hóa đơn thành công!")
                    .data(billRepository.save(bill)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Bill>builder().success(false).message(e.getMessage()).build());
        }
    }

    /** Đánh dấu đã thanh toán (thu tiền mặt) */
    @PostMapping("/{id}/pay-cash")
    public ResponseEntity<ApiResponse<Bill>> payCash(@PathVariable Long id,
                                                     @RequestBody Map<String, Object> body) {
        try {
            Bill bill = billRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));
            bill.setIsPaid(true);
            bill.setPaidAt(LocalDateTime.now());
            bill.setPaidByCash(true);
            if (body.containsKey("collectedByStaffId")) {
                bill.setCollectedByStaffId(Long.valueOf(body.get("collectedByStaffId").toString()));
            }
            return ResponseEntity.ok(ApiResponse.<Bill>builder()
                    .success(true).message("Đã ghi nhận thanh toán!")
                    .data(billRepository.save(bill)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Bill>builder().success(false).message(e.getMessage()).build());
        }
    }
}
