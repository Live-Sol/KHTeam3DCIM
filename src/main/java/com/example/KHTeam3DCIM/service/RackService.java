package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.Rack;
import com.example.KHTeam3DCIM.dto.Rack.RackCreateRequest;
import com.example.KHTeam3DCIM.dto.Rack.RackResponse;
import com.example.KHTeam3DCIM.dto.Rack.RackUpdateRequest;
import com.example.KHTeam3DCIM.repository.DeviceRepository;
import com.example.KHTeam3DCIM.repository.RackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RackService {

    private final RackRepository rackRepository;
    private final DeviceRepository deviceRepository; // [추가] 사용량 계산 위해 필요

    // ==========================================
    // 1. 전체 조회 (사용량 계산 로직 추가)
    // ==========================================
    @Transactional(readOnly = true)
    public List<RackResponse> findAllRacks() {
        return rackRepository.findAll()
                .stream()
                .map(r -> {
                    // DB에서 이 랙의 사용량(높이 합계) 계산해오기
                    Integer used = deviceRepository.getUsedUnitByRackId(r.getId());

                    return RackResponse.builder()
                            .id(r.getId())
                            .rackName(r.getRackName())
                            .totalUnit(r.getTotalUnit())
                            .locationDesc(r.getLocationDesc())
                            .usedUnit(used) // DTO에 담기
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ==========================================
    // 2. 단일 조회 (사용량 계산 로직 추가)
    // ==========================================
    @Transactional(readOnly = true)
    public RackResponse findRackById(Long id) {
        Rack rack = rackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rack이 존재하지 않습니다."));

        Integer used = deviceRepository.getUsedUnitByRackId(rack.getId());

        return RackResponse.builder()
                .id(rack.getId())
                .rackName(rack.getRackName())
                .totalUnit(rack.getTotalUnit())
                .locationDesc(rack.getLocationDesc())
                .usedUnit(used) // DTO에 담기
                .build();
    }

    // ==========================================
    // 3. Rack 추가
    // ==========================================
    public RackResponse addRack(RackCreateRequest request) {
        Rack rack = Rack.builder()
                .rackName(request.getRackName())
                .totalUnit(request.getTotalUnit() != null ? request.getTotalUnit() : 42L)
                .locationDesc(request.getLocationDesc())
                .build();
        Rack saved = rackRepository.save(rack);
        return RackResponse.builder()
                .id(saved.getId())
                .rackName(saved.getRackName())
                .totalUnit(saved.getTotalUnit())
                .locationDesc(saved.getLocationDesc())
                .build();
    }

    // ==========================================
    // 4. Rack 부분 수정
    // ==========================================
    @Transactional
    public RackResponse updateRackPartially(Long id, RackUpdateRequest patch) {
        Rack updated = rackRepository.findById(id)
                .map(existing -> {
                    if (patch.getRackName() != null) existing.setRackName(patch.getRackName());
                    if (patch.getTotalUnit() != null) existing.setTotalUnit(patch.getTotalUnit());
                    if (patch.getLocationDesc() != null) existing.setLocationDesc(patch.getLocationDesc());
                    return rackRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Rack이 존재하지 않습니다."));

        return RackResponse.builder()
                .id(updated.getId())
                .rackName(updated.getRackName())
                .totalUnit(updated.getTotalUnit())
                .locationDesc(updated.getLocationDesc())
                .build();
    }

    // ==========================================
    // 5. Rack 삭제
    // ==========================================
    public void deleteRack(Long id) {
        if (!rackRepository.existsById(id)) {
            throw new RuntimeException("Rack이 존재하지 않습니다.");
        }
        rackRepository.deleteById(id);
    }
}
