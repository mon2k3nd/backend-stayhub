package com.stayhub.api.service.impl;

import com.stayhub.api.entity.Branch;
import com.stayhub.api.exception.ResourceNotFoundException;
import com.stayhub.api.repository.BranchRepository;
import com.stayhub.api.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    @Override
    public List<Branch> getByOwner(Long ownerId) {
        return branchRepository.findByOwnerIdAndIsActiveTrue(ownerId);
    }

    @Override
    public Branch create(Branch branch) {
        return branchRepository.save(branch);
    }

    @Override
    public Branch update(Long id, Branch updated) {
        Branch existing = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dãy trọ ID: " + id));
        existing.setBranchName(updated.getBranchName());
        existing.setAddress(updated.getAddress());
        existing.setDescription(updated.getDescription());
        existing.setCoverImage(updated.getCoverImage());
        return branchRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dãy trọ ID: " + id));
        branch.setIsActive(false);
        branchRepository.save(branch);
    }
}
