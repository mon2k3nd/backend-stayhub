package com.stayhub.api.controller;

import com.stayhub.api.dto.response.ApiResponse;
import com.stayhub.api.entity.StaffAssignment;
import com.stayhub.api.entity.User;
import com.stayhub.api.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StaffController {

    private final StaffService staffService;

    /** Chủ nhà tạo tài khoản nhân viên mới */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<User>> createStaff(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            String phoneNumber = (String) body.get("phoneNumber");
            String password = (String) body.get("password");
            Long ownerId = Long.valueOf(body.get("ownerId").toString());
            List<Long> branchIds = (List<Long>) body.get("branchIds");

            User staff = staffService.createStaff(name, phoneNumber, password, ownerId, branchIds);
            return ResponseEntity.ok(ApiResponse.<User>builder()
                    .success(true).message("Tạo tài khoản nhân viên thành công!")
                    .data(staff).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<User>builder().success(false).message(e.getMessage()).build());
        }
    }

    /** Lấy danh sách nhân viên theo chủ nhà */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<User>>> getStaffByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(ApiResponse.<List<User>>builder()
                .success(true).message("Thành công!")
                .data(staffService.getStaffByOwner(ownerId)).build());
    }

    /** Lấy phân công của nhân viên */
    @GetMapping("/assignments/staff/{staffId}")
    public ResponseEntity<ApiResponse<List<StaffAssignment>>> getAssignments(@PathVariable Long staffId) {
        return ResponseEntity.ok(ApiResponse.<List<StaffAssignment>>builder()
                .success(true).message("Thành công!")
                .data(staffService.getAssignments(staffId)).build());
    }

    /** Cập nhật lịch trực nhân viên */
    @PutMapping("/assignments/{assignmentId}/schedule")
    public ResponseEntity<ApiResponse<StaffAssignment>> updateSchedule(
            @PathVariable Long assignmentId,
            @RequestBody Map<String, String> body) {
        try {
            String schedule = body.get("workSchedule");
            return ResponseEntity.ok(ApiResponse.<StaffAssignment>builder()
                    .success(true).message("Cập nhật lịch trực thành công!")
                    .data(staffService.updateSchedule(assignmentId, schedule)).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<StaffAssignment>builder().success(false).message(e.getMessage()).build());
        }
    }

    /** Xóa nhân viên */
    @DeleteMapping("/{staffId}/owner/{ownerId}")
    public ResponseEntity<ApiResponse<Void>> removeStaff(@PathVariable Long staffId,
                                                          @PathVariable Long ownerId) {
        try {
            staffService.removeStaff(staffId, ownerId);
            return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Đã xóa nhân viên!").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder().success(false).message(e.getMessage()).build());
        }
    }
}
