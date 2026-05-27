package com.stayhub.api.service.impl;

import com.stayhub.api.dto.response.DashboardStats;
import com.stayhub.api.entity.ContractStatus;
import com.stayhub.api.entity.RoomStatus;
import com.stayhub.api.repository.*;
import com.stayhub.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final RoomRepository roomRepository;
    private final ContractRepository contractRepository;
    private final BillRepository billRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public DashboardStats getOwnerStats(Long ownerId, int month, int year) {
        // Phòng
        List<com.stayhub.api.entity.Room> allRooms = roomRepository.findByOwnerId(ownerId);
        long totalRooms = allRooms.size();
        long occupiedRooms = allRooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.DA_THUE)
                .count();
        long emptyRooms = totalRooms - occupiedRooms;

        // Hợp đồng sắp hết hạn (30 ngày)
        LocalDate today = LocalDate.now();
        List<com.stayhub.api.entity.Contract> expiringContracts =
                contractRepository.findExpiringContracts(today, today.plusDays(30));
        long expiringCount = expiringContracts.stream()
                .filter(c -> allRooms.stream().anyMatch(r -> r.getId().equals(c.getRoomId())))
                .count();

        // Doanh thu & chi phí tháng này
        List<com.stayhub.api.entity.Bill> paidBills =
                billRepository.findPaidByMonthYear(month, year);
        double revenue = paidBills.stream()
                .filter(b -> allRooms.stream().anyMatch(r -> r.getId().equals(b.getRoomId())))
                .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0)
                .sum();

        double expenses = expenseRepository.findByOwnerIdAndMonthAndYear(ownerId, month, year)
                .stream()
                .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0)
                .sum();

        // Hóa đơn chưa thanh toán (tất cả tenant của owner)
        List<com.stayhub.api.entity.Bill> allUnpaid = new ArrayList<>();
        for (com.stayhub.api.entity.Room room : allRooms) {
            if (room.getCurrentTenantId() != null) {
                allUnpaid.addAll(billRepository.findUnpaidByTenant(room.getCurrentTenantId()));
            }
        }
        long unpaidCount = allUnpaid.size();
        double unpaidAmount = allUnpaid.stream()
                .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0)
                .sum();

        // Số tenant và chi nhánh
        long totalTenants = allRooms.stream()
                .filter(r -> r.getCurrentTenantId() != null)
                .count();
        long totalBranches = branchRepository.countByOwnerId(ownerId);

        // Biểu đồ doanh thu 6 tháng gần nhất
        List<DashboardStats.MonthlyRevenue> chart = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate d = LocalDate.of(year, month, 1).minusMonths(i);
            int m = d.getMonthValue();
            int y = d.getYear();

            double monthRevenue = billRepository.findPaidByMonthYear(m, y)
                    .stream()
                    .filter(b -> allRooms.stream().anyMatch(r -> r.getId().equals(b.getRoomId())))
                    .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0)
                    .sum();

            double monthExpenses = expenseRepository.findByOwnerIdAndMonthAndYear(ownerId, m, y)
                    .stream()
                    .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0)
                    .sum();

            chart.add(DashboardStats.MonthlyRevenue.builder()
                    .month(m).year(y)
                    .revenue(monthRevenue)
                    .expenses(monthExpenses)
                    .build());
        }

        return DashboardStats.builder()
                .totalRooms(totalRooms)
                .occupiedRooms(occupiedRooms)
                .emptyRooms(emptyRooms)
                .expiringContracts(expiringCount)
                .totalRevenueThisMonth(revenue)
                .totalExpensesThisMonth(expenses)
                .netProfitThisMonth(revenue - expenses)
                .unpaidBillsCount(unpaidCount)
                .unpaidBillsAmount(unpaidAmount)
                .totalTenants(totalTenants)
                .totalBranches(totalBranches)
                .revenueChart(chart)
                .build();
    }
}
