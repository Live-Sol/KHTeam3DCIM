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
    private final DeviceRepository deviceRepository;

    // ==========================================
    // 1. 전체 조회
    // ==========================================
    @Transactional(readOnly = true)
    public List<RackResponse> findAllRacks() {
        return rackRepository.findAll()
                .stream()
                .map(r -> {
                    Integer used = deviceRepository.getUsedUnitByRackId(r.getId());
                    return RackResponse.builder()
                            .id(r.getId())
                            .rackName(r.getRackName())
                            .totalUnit(r.getTotalUnit())
                            .locationDesc(r.getLocationDesc())
                            .usedUnit(used)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ==========================================
    // 2. 단일 조회
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
                .usedUnit(used)
                .build();
    }

    // ⭐ [NEW] 총 랙 개수 조회 메서드 추가
    @Transactional(readOnly = true)
    public long countAllRacks() {
        return rackRepository.count();
    }

    // ==========================================
    // 3. Rack 추가
    // ==========================================
    public RackResponse addRack(RackCreateRequest request) {
        if (rackRepository.existsByRackName(request.getRackName())) {
            throw new IllegalArgumentException("이미 사용 중인 랙 이름입니다.");
        }

        Rack rack = Rack.builder()
                .rackName(request.getRackName())
                .totalUnit(request.getTotalUnit() != null ? request.getTotalUnit() : 42L)
                .locationDesc(request.getLocationDesc())
                .build();

        Rack saved = rackRepository.save(rack);
        return toResponse(saved);
    }

    // ==========================================
    // 4. Rack 수정 (정교한 검증)
    // ==========================================
    public RackResponse updateRackPartially(Long id, RackUpdateRequest patch) {
        Rack existing = rackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 Rack이 존재하지 않습니다."));

        // 1. 이름 변경 시 중복 체크
        if (patch.getRackName() != null && !existing.getRackName().equals(patch.getRackName())) {
            if (rackRepository.existsByRackName(patch.getRackName())) {
                throw new IllegalArgumentException("이미 사용 중인 랙 이름입니다.");
            }
            existing.setRackName(patch.getRackName());
        }

        // 2. 높이 수정 시 물리적 실장 상태 확인
        if (patch.getTotalUnit() != null) {
            // [수정] getMaxUnitByRackId가 이제 COALESCE 덕분에 0 이상을 반환함
            Integer usedMaxUnit = deviceRepository.getMaxUnitByRackId(id);

            if (usedMaxUnit > 0 && patch.getTotalUnit() < usedMaxUnit) {
                throw new IllegalArgumentException(
                        String.format("현재 %dU 위치에 장비가 실장되어 있어 높이를 그 미만으로 줄일 수 없습니다.", usedMaxUnit)
                );
            }
            existing.setTotalUnit(patch.getTotalUnit());
        }

        if (patch.getLocationDesc() != null) {
            existing.setLocationDesc(patch.getLocationDesc());
        }

        return toResponse(existing); // Dirty Checking으로 자동 반영됨
    }

    // ==========================================
    // 5. Rack 삭제 (안전장치 추가)
    // ==========================================
    public void deleteRack(Long id) {
        if (!rackRepository.existsById(id)) {
            throw new RuntimeException("삭제할 Rack이 존재하지 않습니다.");
        }

        // [수정] 장비가 들어있는 랙은 함부로 삭제하면 데이터가 꼬입니다.
        Integer usedUnitCount = deviceRepository.getUsedUnitByRackId(id);
        if (usedUnitCount > 0) {
            throw new IllegalStateException("장비가 설치된 랙은 삭제할 수 없습니다. 먼저 장비를 제거하거나 이동해주세요.");
        }

        rackRepository.deleteById(id);
    }

    // 공통 응답 변환 로직 (UsedUnit 합산 포함)
    private RackResponse toResponse(Rack rack) {
        return RackResponse.builder()
                .id(rack.getId())
                .rackName(rack.getRackName())
                .totalUnit(rack.getTotalUnit())
                .locationDesc(rack.getLocationDesc())
                .usedUnit(deviceRepository.getUsedUnitByRackId(rack.getId()))
                .build();
    }
}