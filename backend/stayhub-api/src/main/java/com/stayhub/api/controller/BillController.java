package com.stayhub.api.controller;

import com.stayhub.api.dto.response.ApiResponse;
import com.stayhub.api.entity.Bill;
import com.stayhub.api.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    /** Lấy tất cả hóa đơn của khách thuê */
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<List<Bill>>> getByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(ApiResponse.<List<Bill>>builder()
                .success(true).message("Thành công!")
                .data(billService.getByTenant(tenantId)).build());
    }

    /** Lấy hóa đơn chưa thanh toán của khách */
    @GetMapping("/tenant/{tenantId}/unpaid")
    public ResponseEntity<ApiResponse<List<Bill>>> getUnpaid(@PathVariable Long tenantId) {
        return ResponseEntity.ok(ApiResponse.<List<Bill>>builder()
                .success(true).message("Thành công!")
                .data(billService.getUnpaidByTenant(tenantId)).build());
    }

    /** Lấy hóa đơn theo phòng */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<List<Bill>>> getByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.<List<Bill>>builder()
                .success(true).message("Thành công!")
                .data(billService.getByRoom(roomId)).build());
    }

    /** Chủ nhà / Nhân viên tạo hóa đơn tháng. Giá điện/nước lấy từ hợp đồng. */
    @PostMapping
    public ResponseEntity<ApiResponse<Bill>> createBill(@RequestBody Bill bill) {
        Bill saved = billService.createBill(bill);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Bill>builder()
                        .success(true).message("Tạo hóa đơn thành công!")
                        .data(saved).build());
    }

    /** Đánh dấu đã thanh toán tiền mặt */
    @PostMapping("/{id}/pay-cash")
    public ResponseEntity<ApiResponse<Bill>> payCash(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        Long staffId = null;
        if (body != null && body.containsKey("collectedByStaffId")) {
            staffId = Long.valueOf(body.get("collectedByStaffId").toString());
        }
        Bill updated = billService.payCash(id, staffId);
        return ResponseEntity.ok(ApiResponse.<Bill>builder()
                .success(true).message("Đã ghi nhận thanh toán!")
                .data(updated).build());
    }
}
