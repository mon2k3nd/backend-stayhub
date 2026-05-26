package com.stayhub.api.service;

import com.stayhub.api.entity.Branch;
import java.util.List;

public interface BranchService {
    List<Branch> getByOwner(Long ownerId);
    Branch create(Branch branch);
    Branch update(Long id, Branch branch);
    void delete(Long id);
}
