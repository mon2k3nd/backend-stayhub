package com.stayhub.api.service;

import com.stayhub.api.entity.Bill;

import java.util.List;

public interface BillService {

    List<Bill> getByTenant(Long tenantId);

    List<Bill> getUnpaidByTenant(Long tenantId);

    List<Bill> getByRoom(Long roomId);

    Bill createBill(Bill bill);

    Bill payCash(Long billId, Long collectedByStaffId);

    Bill getById(Long billId);
}
