package com.stayhub.api.service.impl;

import com.stayhub.api.entity.*;
import com.stayhub.api.exception.ResourceNotFoundException;
import com.stayhub.api.repository.ContractRepository;
import com.stayhub.api.repository.RoomRepository;
import com.stayhub.api.service.ContractService;
import com.stayhub.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;

    @Override
    public List<Contract> getByTenant(Long tenantId) {
        return contractRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Contract> getByOwner(Long ownerId) {
        return contractRepository.findByOwnerId(ownerId);
    }

    @Override
    public Contract getActiveByRoom(Long roomId) {
        return contractRepository.findByRoomIdAndStatusIn(roomId,
                Arrays.asList(ContractStatus.ACTIVE, ContractStatus.PENDING))
                .orElseThrow(() -> new ResourceNotFoundException("Không có hợp đồng đang hoạt động cho phòng này"));
    }

    @Override
    @Transactional
    public Contract create(Contract contract) {
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setSignedAt(LocalDateTime.now());
        Contract saved = contractRepository.save(contract);

        // Cập nhật trạng thái phòng -> DA_THUE
        Room room = roomRepository.findById(contract.getRoomId()).orElse(null);
        if (room != null) {
            room.setStatus(RoomStatus.DA_THUE);
            room.setCurrentTenantId(contract.getTenantId());
            room.setCurrentContractId(saved.getId());
            roomRepository.save(room);
        }

        // Gửi thông báo cho tenant
        notificationService.send(contract.getTenantId(),
                "Hợp đồng đã được ký kết!",
                "Chào mừng bạn đến ở. Hợp đồng của bạn có hiệu lực từ " + contract.getStartDate(),
                NotificationType.CONTRACT_SIGNED, saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Contract terminate(Long id, String reason) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hợp đồng ID: " + id));
        contract.setStatus(ContractStatus.TERMINATED);
        contract.setTerminatedAt(LocalDateTime.now());
        contract.setTerminationReason(reason);

        // Giải phóng phòng
        freeRoom(contract.getRoomId());

        notificationService.send(contract.getTenantId(),
                "Hợp đồng đã bị chấm dứt",
                "Hợp đồng của bạn đã bị chấm dứt. Lý do: " + reason,
                NotificationType.CONTRACT_TERMINATED, id);
        return contractRepository.save(contract);
    }

    @Override
    @Transactional
    public Contract renew(Long id, int months) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hợp đồng ID: " + id));
        contract.setEndDate(contract.getEndDate().plusMonths(months));
        contract.setStatus(ContractStatus.ACTIVE);
        return contractRepository.save(contract);
    }

    @Override
    @Transactional
    public Map<String, Object> liquidate(Long id, Map<String, Object> data) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hợp đồng ID: " + id));

        double damageDeduction = data.containsKey("damageDeduction")
                ? Double.parseDouble(data.get("damageDeduction").toString()) : 0.0;
        double depositRefund = (contract.getDepositAmount() != null ? contract.getDepositAmount() : 0) - damageDeduction;

        contract.setStatus(ContractStatus.LIQUIDATED);
        contract.setTerminatedAt(LocalDateTime.now());
        contractRepository.save(contract);

        freeRoom(contract.getRoomId());

        Map<String, Object> result = new HashMap<>();
        result.put("depositRefund", Math.max(depositRefund, 0));
        result.put("damageDeduction", damageDeduction);
        result.put("message", "Thanh lý hợp đồng thành công. Hoàn trả cọc: " + depositRefund + " VNĐ");
        return result;
    }

    private void freeRoom(Long roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room != null) {
            room.setStatus(RoomStatus.TRONG);
            room.setCurrentTenantId(null);
            room.setCurrentContractId(null);
            roomRepository.save(room);
        }
    }
}
