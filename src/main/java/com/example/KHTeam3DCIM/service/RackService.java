package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.LogType; // [로그 추가] Enum 필요
import com.example.KHTeam3DCIM.domain.Rack;
import com.example.KHTeam3DCIM.dto.Rack.RackCreateRequest;
import com.example.KHTeam3DCIM.dto.Rack.RackResponse;
import com.example.KHTeam3DCIM.dto.Rack.RackUpdateRequest;
import com.example.KHTeam3DCIM.repository.DeviceRepository;
import com.example.KHTeam3DCIM.repository.RackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder; // [로그 추가] 현재 사용자 ID 획득용
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
    private final AuditLogService auditLogService; // [로그 추가] 로그 서비스 주입

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

        // [로그 수정] 구체적인 문장형 로그
        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();

        // 예: "42U의 ServerRack-A이(가) 전산실 101호에 생성되었습니다."
        String logMessage = String.format("%dU의 %s이(가) %s에 생성되었습니다.",
                saved.getTotalUnit(),
                saved.getRackName(),
                saved.getLocationDesc() != null ? saved.getLocationDesc() : "위치미지정");

        auditLogService.saveLog(currentMemberId, logMessage, LogType.DEVICE_OPERATION);

        return toResponse(saved);
    }

    // ==========================================
    // 4. Rack 수정
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

        // [로그 수정] 수정 내역 로그
        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();

        // 예: "42U의 ServerRack-A (전산실 101호) 정보가 수정되었습니다."
        String logMessage = String.format("%dU의 %s (%s) 정보가 수정되었습니다.",
                existing.getTotalUnit(),
                existing.getRackName(),
                existing.getLocationDesc() != null ? existing.getLocationDesc() : "위치미지정");

        auditLogService.saveLog(currentMemberId, logMessage, LogType.DEVICE_OPERATION);

        return toResponse(existing);
    }

    // ==========================================
    // 5. Rack 삭제
    // ==========================================
    public void deleteRack(Long id) {
        Rack rack = rackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("삭제할 Rack이 존재하지 않습니다."));

        Integer usedUnitCount = deviceRepository.getUsedUnitByRackId(id);
        if (usedUnitCount > 0) {
            throw new IllegalStateException("장비가 설치된 랙은 삭제할 수 없습니다. 먼저 장비를 제거하거나 이동해주세요.");
        }

        rackRepository.delete(rack);

        // [로그 수정] 삭제 내역 로그 (삭제는 과거형 문맥 강조)
        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();

        // 예: "전산실 101호에 위치했던 42U의 ServerRack-A이(가) 삭제되었습니다."
        String logMessage = String.format("%s에 위치했던 %dU의 %s이(가) 삭제되었습니다.",
                rack.getLocationDesc() != null ? rack.getLocationDesc() : "위치미지정",
                rack.getTotalUnit(),
                rack.getRackName());

        auditLogService.saveLog(currentMemberId, logMessage, LogType.DEVICE_OPERATION);
    }

    // 공통 응답 변환 로직
    private RackResponse toResponse(Rack rack) {
        return RackResponse.builder()
                .id(rack.getId())
                .rackName(rack.getRackName())
                .totalUnit(rack.getTotalUnit())
                .locationDesc(rack.getLocationDesc())
                .usedUnit(deviceRepository.getUsedUnitByRackId(rack.getId()))
                .build();
    }

//    // ==========================================
//    // 3. Rack 추가
//    // ==========================================
//    public RackResponse addRack(RackCreateRequest request) {
//        if (rackRepository.existsByRackName(request.getRackName())) {
//            throw new IllegalArgumentException("이미 사용 중인 랙 이름입니다.");
//        }
//
//        Rack rack = Rack.builder()
//                .rackName(request.getRackName())
//                .totalUnit(request.getTotalUnit() != null ? request.getTotalUnit() : 42L)
//                .locationDesc(request.getLocationDesc())
//                .build();
//
//        Rack saved = rackRepository.save(rack);
//
//        // [로그 추가] 생성 로그 기록
//        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
//        auditLogService.saveLog(currentMemberId, "Rack 생성: " + saved.getRackName(), LogType.DEVICE_OPERATION);
//
//        return toResponse(saved);
//    }
//
//    // ==========================================
//    // 4. Rack 수정 (정교한 검증)
//    // ==========================================
//    public RackResponse updateRackPartially(Long id, RackUpdateRequest patch) {
//        Rack existing = rackRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("해당 Rack이 존재하지 않습니다."));
//
//        // 1. 이름 변경 시 중복 체크
//        if (patch.getRackName() != null && !existing.getRackName().equals(patch.getRackName())) {
//            if (rackRepository.existsByRackName(patch.getRackName())) {
//                throw new IllegalArgumentException("이미 사용 중인 랙 이름입니다.");
//            }
//            existing.setRackName(patch.getRackName());
//        }
//
//        // 2. 높이 수정 시 물리적 실장 상태 확인
//        if (patch.getTotalUnit() != null) {
//            Integer usedMaxUnit = deviceRepository.getMaxUnitByRackId(id);
//
//            if (usedMaxUnit > 0 && patch.getTotalUnit() < usedMaxUnit) {
//                throw new IllegalArgumentException(
//                        String.format("현재 %dU 위치에 장비가 실장되어 있어 높이를 그 미만으로 줄일 수 없습니다.", usedMaxUnit)
//                );
//            }
//            existing.setTotalUnit(patch.getTotalUnit());
//        }
//
//        if (patch.getLocationDesc() != null) {
//            existing.setLocationDesc(patch.getLocationDesc());
//        }
//
//        // [로그 추가] 수정 로그 기록 (Dirty Checking으로 DB 반영은 트랜잭션 종료 시점이지만, 로그는 여기서 남김)
//        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
//        auditLogService.saveLog(currentMemberId, "Rack 수정: " + existing.getRackName(), LogType.DEVICE_OPERATION);
//
//        return toResponse(existing);
//    }
//
//    // ==========================================
//    // 5. Rack 삭제 (안전장치 추가)
//    // ==========================================
//    public void deleteRack(Long id) {
//        // [로그 추가를 위해 수정] 삭제할 랙의 이름을 로그에 남기기 위해 existsById 대신 findById 사용
//        Rack rack = rackRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("삭제할 Rack이 존재하지 않습니다."));
//
//        // [수정] 장비가 들어있는 랙은 함부로 삭제하면 데이터가 꼬입니다.
//        Integer usedUnitCount = deviceRepository.getUsedUnitByRackId(id);
//        if (usedUnitCount > 0) {
//            throw new IllegalStateException("장비가 설치된 랙은 삭제할 수 없습니다. 먼저 장비를 제거하거나 이동해주세요.");
//        }
//
//        rackRepository.delete(rack); // deleteById 대신 객체로 삭제 (기능 동일)
//
//        // [로그 추가] 삭제 로그 기록
//        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
//        auditLogService.saveLog(currentMemberId, "Rack 삭제: " + rack.getRackName(), LogType.DEVICE_OPERATION);
//    }
//
//    // 공통 응답 변환 로직 (UsedUnit 합산 포함)
//    private RackResponse toResponse(Rack rack) {
//        return RackResponse.builder()
//                .id(rack.getId())
//                .rackName(rack.getRackName())
//                .totalUnit(rack.getTotalUnit())
//                .locationDesc(rack.getLocationDesc())
//                .usedUnit(deviceRepository.getUsedUnitByRackId(rack.getId()))
//                .build();
//    }
}