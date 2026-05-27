package com.stayhub.api.dto.response;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStats {

    private long totalRooms;
    private long occupiedRooms;
    private long emptyRooms;
    private long expiringContracts;

    private double totalRevenueThisMonth;
    private double totalExpensesThisMonth;
    private double netProfitThisMonth;

    private long unpaidBillsCount;
    private double unpaidBillsAmount;

    private long totalTenants;
    private long totalBranches;

    private List<MonthlyRevenue> revenueChart;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MonthlyRevenue {
        private int month;
        private int year;
        private double revenue;
        private double expenses;
    }
}
