package com.stayhub.api.service.impl;

import com.stayhub.api.entity.Bill;
import com.stayhub.api.entity.Contract;
import com.stayhub.api.exception.ResourceNotFoundException;
import com.stayhub.api.repository.BillRepository;
import com.stayhub.api.repository.ContractRepository;
import com.stayhub.api.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final ContractRepository contractRepository;

    @Override
    public List<Bill> getByTenant(Long tenantId) {
        return billRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Override
    public List<Bill> getUnpaidByTenant(Long tenantId) {
        return billRepository.findUnpaidByTenant(tenantId);
    }

    @Override
    public List<Bill> getByRoom(Long roomId) {
        return billRepository.findByRoomId(roomId);
    }

    @Override
    @Transactional
    public Bill createBill(Bill bill) {
        // Lấy giá điện/nước từ hợp đồng nếu bill không cung cấp
        if (bill.getContractId() != null) {
            contractRepository.findById(bill.getContractId()).ifPresent(contract -> {
                applyContractPrices(bill, contract);
            });
        } else if (bill.getRoomId() != null) {
            // Fallback: tìm hợp đồng đang active của phòng
            contractRepository
                    .findByRoomIdAndStatusIn(bill.getRoomId(),
                            List.of(com.stayhub.api.entity.ContractStatus.ACTIVE))
                    .ifPresent(contract -> applyContractPrices(bill, contract));
        }

        // Tính tiền điện
        if (bill.getElectricCurrent() != null && bill.getElectricPrevious() != null) {
            double electricPrice = bill.getElectricUnitPrice() != null
                    ? bill.getElectricUnitPrice()
                    : 3500.0;
            bill.setElectricAmount(
                    (bill.getElectricCurrent() - bill.getElectricPrevious()) * electricPrice);
        }

        // Tính tiền nước
        if (bill.getWaterCurrent() != null && bill.getWaterPrevious() != null) {
            double waterPrice = bill.getWaterUnitPrice() != null
                    ? bill.getWaterUnitPrice()
                    : 15000.0;
            bill.setWaterAmount(
                    (bill.getWaterCurrent() - bill.getWaterPrevious()) * waterPrice);
        }

        // Tính tổng
        double total = 0;
        if (bill.getRentAmount() != null) total += bill.getRentAmount();
        if (bill.getElectricAmount() != null) total += bill.getElectricAmount();
        if (bill.getWaterAmount() != null) total += bill.getWaterAmount();
        if (bill.getServiceAmount() != null) total += bill.getServiceAmount();
        bill.setTotalAmount(total);

        if (bill.getDueDate() == null) {
            bill.setDueDate(LocalDateTime.now().plusDays(10));
        }

        return billRepository.save(bill);
    }

    @Override
    @Transactional
    public Bill payCash(Long billId, Long collectedByStaffId) {
        Bill bill = getById(billId);
        if (Boolean.TRUE.equals(bill.getIsPaid())) {
            throw new IllegalStateException("Hóa đơn này đã được thanh toán trước đó!");
        }
        bill.setIsPaid(true);
        bill.setPaidAt(LocalDateTime.now());
        bill.setPaidByCash(true);
        bill.setCollectedByStaffId(collectedByStaffId);
        return billRepository.save(bill);
    }

    @Override
    public Bill getById(Long billId) {
        return billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID: " + billId));
    }

    private void applyContractPrices(Bill bill, Contract contract) {
        // Dùng giá từ hợp đồng làm giá đơn vị mặc định
        if (bill.getElectricUnitPrice() == null && contract.getElectricityPrice() != null) {
            bill.setElectricUnitPrice(contract.getElectricityPrice());
        }
        if (bill.getWaterUnitPrice() == null && contract.getWaterPrice() != null) {
            bill.setWaterUnitPrice(contract.getWaterPrice());
        }
        if (bill.getRentAmount() == null && contract.getMonthlyRent() != null) {
            bill.setRentAmount(contract.getMonthlyRent());
        }
        if (bill.getServiceAmount() == null && contract.getServiceFee() != null) {
            bill.setServiceAmount(contract.getServiceFee());
        }
        if (bill.getTenantId() == null) {
            bill.setTenantId(contract.getTenantId());
        }
    }
}
