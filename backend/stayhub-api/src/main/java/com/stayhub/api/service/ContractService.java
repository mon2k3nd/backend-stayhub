package com.stayhub.api.service;

import com.stayhub.api.entity.Contract;
import java.util.List;
import java.util.Map;

public interface ContractService {
    List<Contract> getByTenant(Long tenantId);
    List<Contract> getByOwner(Long ownerId);
    Contract getActiveByRoom(Long roomId);
    Contract create(Contract contract);
    Contract terminate(Long id, String reason);
    Contract renew(Long id, int months);
    Map<String, Object> liquidate(Long id, Map<String, Object> data);
}
