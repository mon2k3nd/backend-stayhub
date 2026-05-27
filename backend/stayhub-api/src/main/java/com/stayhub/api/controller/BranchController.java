package com.stayhub.api.controller;

import com.stayhub.api.dto.response.ApiResponse;
import com.stayhub.api.entity.Branch;
import com.stayhub.api.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<Branch>>> getByOwner(@PathVariable Long ownerId) {
        try {
            return ResponseEntity.ok(ApiResponse.<List<Branch>>builder()
                    .success(true).message("Thành công!")
                    .data(branchService.getByOwner(ownerId)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<Branch>>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Branch>> create(@RequestBody Branch branch) {
        try {
            return ResponseEntity.ok(ApiResponse.<Branch>builder()
                    .success(true).message("Tạo dãy trọ thành công!")
                    .data(branchService.create(branch)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Branch>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Branch>> update(@PathVariable Long id, @RequestBody Branch branch) {
        try {
            return ResponseEntity.ok(ApiResponse.<Branch>builder()
                    .success(true).message("Cập nhật thành công!")
                    .data(branchService.update(id, branch)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Branch>builder().success(false).message(e.getMessage()).build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            branchService.delete(id);
            return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Đã xóa dãy trọ!").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder().success(false).message(e.getMessage()).build());
        }
    }
}
