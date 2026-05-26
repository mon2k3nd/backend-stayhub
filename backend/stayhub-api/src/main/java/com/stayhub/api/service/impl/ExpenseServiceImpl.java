package com.stayhub.api.service.impl;

import com.stayhub.api.entity.Expense;
import com.stayhub.api.repository.BillRepository;
import com.stayhub.api.repository.ExpenseRepository;
import com.stayhub.api.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BillRepository billRepository;

    @Override
    public List<Expense> getByOwner(Long ownerId) {
        return expenseRepository.findByOwnerIdOrderByExpenseDateDesc(ownerId);
    }

    @Override
    public Expense create(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Override
    public Map<String, Object> profitReport(Long ownerId, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        // Tổng chi phí phát sinh
        Double totalExpense = expenseRepository.sumByOwnerIdAndDateRange(ownerId, from, to);
        if (totalExpense == null) totalExpense = 0.0;

        // Tổng doanh thu từ hóa đơn đã thu trong tháng
        List<?> paidBills = billRepository.findAll().stream()
                .filter(b -> {
                    com.stayhub.api.entity.Bill bill = (com.stayhub.api.entity.Bill) b;
                    return Boolean.TRUE.equals(bill.getIsPaid())
                            && bill.getMonth() == month
                            && bill.getYear() == year;
                }).toList();

        double totalRevenue = paidBills.stream()
                .mapToDouble(b -> {
                    com.stayhub.api.entity.Bill bill = (com.stayhub.api.entity.Bill) b;
                    return bill.getTotalAmount() != null ? bill.getTotalAmount() : 0.0;
                }).sum();

        double netProfit = totalRevenue - totalExpense;

        Map<String, Object> report = new HashMap<>();
        report.put("month", month);
        report.put("year", year);
        report.put("totalRevenue", totalRevenue);
        report.put("totalExpense", totalExpense);
        report.put("netProfit", netProfit);
        report.put("profitMargin", totalRevenue > 0 ? (netProfit / totalRevenue * 100) : 0);
        return report;
    }
}
