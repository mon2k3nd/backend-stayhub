package com.stayhub.api.controller;

import com.stayhub.api.dto.response.ApiResponse;
import com.stayhub.api.entity.Roommate;
import com.stayhub.api.service.RoommateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roommates")
@RequiredArgsConstructor
public class RoommateController {

    private final RoommateService roommateService;

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<ApiResponse<List<Roommate>>> getByContract(@PathVariable Long contractId) {
        return ResponseEntity.ok(ApiResponse.<List<Roommate>>builder()
                .success(true).message("Thành công!")
                .data(roommateService.getByContract(contractId)).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Roommate>> addRoommate(@RequestBody Roommate roommate) {
        try {
            return ResponseEntity.ok(ApiResponse.<Roommate>builder()
                    .success(true).message("Thêm bạn cùng phòng thành công!")
                    .data(roommateService.addRoommate(roommate)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Roommate>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<ApiResponse<Void>> checkout(@PathVariable Long id) {
        try {
            roommateService.checkout(id);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true).message("Đã xóa người ở cùng!").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder().success(false).message(e.getMessage()).build());
        }
    }
}
