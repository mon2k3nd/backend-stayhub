package com.stayhub.api.controller;

import com.stayhub.api.dto.response.ApiResponse;
import com.stayhub.api.entity.Expense;
import com.stayhub.api.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<Expense>>> getByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(ApiResponse.<List<Expense>>builder()
                .success(true).message("Thành công!")
                .data(expenseService.getByOwner(ownerId)).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Expense>> create(@RequestBody Expense expense) {
        try {
            return ResponseEntity.ok(ApiResponse.<Expense>builder()
                    .success(true).message("Ghi chi phí thành công!")
                    .data(expenseService.create(expense)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Expense>builder().success(false).message(e.getMessage()).build());
        }
    }

    /** Báo cáo lợi nhuận ròng theo tháng */
    @GetMapping("/owner/{ownerId}/profit-report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> profitReport(
            @PathVariable Long ownerId,
            @RequestParam int month,
            @RequestParam int year) {
        try {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true).message("Thành công!")
                    .data(expenseService.profitReport(ownerId, month, year)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Map<String, Object>>builder().success(false).message(e.getMessage()).build());
        }
    }
}
