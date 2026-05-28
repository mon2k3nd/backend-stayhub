package com.stayhub.api.service.impl;

import com.stayhub.api.entity.*;
import com.stayhub.api.exception.ResourceNotFoundException;
import com.stayhub.api.repository.*;
import com.stayhub.api.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;
    private final StaffAssignmentRepository staffAssignmentRepository;
    private final OwnerTenantMappingRepository ownerTenantMappingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createStaff(String name, String phoneNumber, String password,
                            Long ownerId, List<Long> branchIds) {
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalStateException("Số điện thoại đã tồn tại trong hệ thống!");
        }

        User staff = User.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email("staff_" + phoneNumber + "@stayhub.com")
                .passwordHash(passwordEncoder.encode(password))
                .roleId("STAFF")
                .accountStatus("ACTIVE")
                .build();
        User savedStaff = userRepository.save(staff);

        // Liên kết owner - staff
        OwnerTenantMapping mapping = new OwnerTenantMapping();
        mapping.setOwnerId(ownerId);
        mapping.setTenantId(savedStaff.getId());
        ownerTenantMappingRepository.save(mapping);

        // Phân công theo từng dãy trọ
        if (branchIds != null) {
            for (Long branchId : branchIds) {
                StaffAssignment assignment = StaffAssignment.builder()
                        .staffId(savedStaff.getId())
                        .ownerId(ownerId)
                        .branchId(branchId)
                        .isActive(true)
                        .build();
                staffAssignmentRepository.save(assignment);
            }
        }

        return savedStaff;
    }

    @Override
    public List<User> getStaffByOwner(Long ownerId) {
        List<StaffAssignment> assignments = staffAssignmentRepository.findByOwnerIdAndIsActiveTrue(ownerId);
        List<Long> staffIds = assignments.stream()
                .map(StaffAssignment::getStaffId)
                .distinct()
                .collect(Collectors.toList());
        return (List<User>) userRepository.findAllById(staffIds);
    }

    @Override
    public List<StaffAssignment> getAssignments(Long staffId) {
        return staffAssignmentRepository.findByStaffIdAndIsActiveTrue(staffId);
    }

    @Override
    public StaffAssignment updateSchedule(Long assignmentId, String schedule) {
        StaffAssignment assignment = staffAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công ID: " + assignmentId));
        assignment.setWorkSchedule(schedule);
        return staffAssignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public void removeStaff(Long staffId, Long ownerId) {
        staffAssignmentRepository.findByStaffIdAndIsActiveTrue(staffId).forEach(a -> {
            a.setIsActive(false);
            staffAssignmentRepository.save(a);
        });
    }
}
