package com.stayhub.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StaffAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    // Lịch trực: MON,TUE,WED,THU,FRI,SAT,SUN
    @Column(name = "work_schedule")
    private String workSchedule;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "assigned_at")
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();
}
