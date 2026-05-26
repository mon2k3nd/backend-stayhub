package com.stayhub.api.service;

import com.stayhub.api.entity.Expense;
import java.util.List;
import java.util.Map;

public interface ExpenseService {
    List<Expense> getByOwner(Long ownerId);
    Expense create(Expense expense);
    Map<String, Object> profitReport(Long ownerId, int month, int year);
}
