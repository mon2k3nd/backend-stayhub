package com.stayhub.api.controller;

import com.stayhub.api.dto.response.ApiResponse;
import com.stayhub.api.entity.Contract;
import com.stayhub.api.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<List<Contract>>> getByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(ApiResponse.<List<Contract>>builder()
                .success(true).message("Thành công!")
                .data(contractService.getByTenant(tenantId)).build());
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<Contract>>> getByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(ApiResponse.<List<Contract>>builder()
                .success(true).message("Thành công!")
                .data(contractService.getByOwner(ownerId)).build());
    }

    @GetMapping("/room/{roomId}/active")
    public ResponseEntity<ApiResponse<Contract>> getActiveByRoom(@PathVariable Long roomId) {
        try {
            return ResponseEntity.ok(ApiResponse.<Contract>builder()
                    .success(true).message("Thành công!")
                    .data(contractService.getActiveByRoom(roomId)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Contract>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Contract>> create(@RequestBody Contract contract) {
        try {
            return ResponseEntity.ok(ApiResponse.<Contract>builder()
                    .success(true).message("Tạo hợp đồng thành công!")
                    .data(contractService.create(contract)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Contract>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/{id}/terminate")
    public ResponseEntity<ApiResponse<Contract>> terminate(@PathVariable Long id,
                                                           @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "Chấm dứt hợp đồng theo yêu cầu");
            return ResponseEntity.ok(ApiResponse.<Contract>builder()
                    .success(true).message("Đã chấm dứt hợp đồng!")
                    .data(contractService.terminate(id, reason)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Contract>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<ApiResponse<Contract>> renew(@PathVariable Long id,
                                                       @RequestBody Map<String, String> body) {
        try {
            int months = Integer.parseInt(body.getOrDefault("months", "12"));
            return ResponseEntity.ok(ApiResponse.<Contract>builder()
                    .success(true).message("Gia hạn hợp đồng thành công!")
                    .data(contractService.renew(id, months)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Contract>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/{id}/liquidate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> liquidate(@PathVariable Long id,
                                                                       @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true).message("Thanh lý hợp đồng thành công!")
                    .data(contractService.liquidate(id, body)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Map<String, Object>>builder().success(false).message(e.getMessage()).build());
        }
    }
}
