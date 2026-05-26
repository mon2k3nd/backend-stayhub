package com.stayhub.api.service.impl;

import com.stayhub.api.entity.RoomAsset;
import com.stayhub.api.exception.ResourceNotFoundException;
import com.stayhub.api.repository.RoomAssetRepository;
import com.stayhub.api.service.RoomAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomAssetServiceImpl implements RoomAssetService {

    private final RoomAssetRepository roomAssetRepository;

    @Override
    public List<RoomAsset> getByRoom(Long roomId) {
        return roomAssetRepository.findByRoomId(roomId);
    }

    @Override
    public RoomAsset create(RoomAsset asset) {
        asset.setCreatedAt(LocalDateTime.now());
        asset.setUpdatedAt(LocalDateTime.now());
        return roomAssetRepository.save(asset);
    }

    @Override
    public RoomAsset update(Long id, RoomAsset updated) {
        RoomAsset existing = roomAssetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài sản ID: " + id));
        existing.setAssetName(updated.getAssetName());
        existing.setQuantity(updated.getQuantity());
        existing.setConditionNote(updated.getConditionNote());
        existing.setAssetStatus(updated.getAssetStatus());
        existing.setValue(updated.getValue());
        existing.setUpdatedAt(LocalDateTime.now());
        return roomAssetRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        roomAssetRepository.deleteById(id);
    }
}
