package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.*;
import com.example.KHTeam3DCIM.dto.Rack.RackDetailDto;
import com.example.KHTeam3DCIM.dto.device.deviceDTO;
import com.example.KHTeam3DCIM.repository.CategoryRepository;
import com.example.KHTeam3DCIM.repository.DeviceRepository;
import com.example.KHTeam3DCIM.repository.RackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final RackRepository rackRepository;
    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;
    private final EnvironmentService envService; // 장비가 변경될 때 환경 정보도 업데이트되도록 장비 저장/삭제 후 호출

    // ==========================================
    // 1. 장비 등록하기
    // ==========================================
    @Transactional
    public Long registerDevice(Long rackId, String cateId, Device newDevice) {
        // 공통 검증 메서드 호출 (새 장비이므로 currentDeviceId는 null)
        checkRackOverlap(rackId, newDevice.getStartUnit(), newDevice.getHeightUnit(), null);

        Rack rack = rackRepository.findById(rackId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 랙입니다."));
        Category category = categoryRepository.findById(cateId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        // 위치 충돌 체크
        List<Device> existingDevices = deviceRepository.findByRackId(rackId);
        int newStart = newDevice.getStartUnit();
        int newEnd = newStart + newDevice.getHeightUnit() - 1;

        if (newEnd > rack.getTotalUnit()) {
            throw new IllegalStateException("장비가 랙 높이를 벗어납니다.");
        }

        for (Device existing : existingDevices) {
            int exStart = existing.getStartUnit();
            int exEnd = exStart + existing.getHeightUnit() - 1;
            if (newStart <= exEnd && newEnd >= exStart) {
                throw new IllegalStateException("이미 해당 위치(" + exStart + "~" + exEnd + "U)에 장비가 있습니다.");
            }
        }

        newDevice.setRack(rack);
        newDevice.setCategory(category);

        if (newDevice.getStatus() == null || newDevice.getStatus().isEmpty()) {
            newDevice.setStatus("OFF");
        }

        deviceRepository.save(newDevice);

        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.saveLog(currentMemberId, "장비 등록: " + newDevice.getSerialNum(), LogType.DEVICE_OPERATION);

        return newDevice.getId();
    }

    // ==========================================
    // 2. 조회 기능들
    // ==========================================
    private Sort createSort(String sortOption, String sortDir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String property = switch (sortOption) {
            case "id_asc" -> "id";
            case "member" -> "member.memberId"; // [추가] 신청자 아이디 기준 정렬
            case "rack" -> "rack.rackName";
            case "category" -> "category.name";
            case "serial" -> "serialNum";
            case "location" -> "startUnit";
            case "status" -> "status";
            case "contract", "expiry" -> "contractDate";
            default -> "id";
        };
        return Sort.by(direction, property);
    }

    // ==========================================
    // 3. 에너지 대시보드용 통계 데이터 생성
    // ==========================================
    public Map<String, Object> getEnergyStatistics() {
        // 1. IT 장비 총 전력 (DB에서 조회)
        long itPower = deviceRepository.sumTotalPower();

        // 2. 기반 설비 전력 (가정: IT 전력의 0.5배만큼 냉방비로 더 쓴다고 가정 -> 총 1.5배)
        // 실제로는 센서가 필요하지만, 시뮬레이션이므로 공식으로 계산합니다.
        long facilityPower = (long) (itPower * 1.5);

        // 3. PUE 계산 공식: (총 전력 / IT 전력)
        // IT 전력이 0이면 나눗셈 에러나므로 1.0(이상적 수치)으로 처리
        double pue = (itPower == 0) ? 1.0 : (double) facilityPower / itPower;

        // 4. 데이터를 맵(Map)이라는 보따리에 담아서 리턴
        Map<String, Object> stats = new HashMap<>();
        stats.put("itPower", itPower);         // IT 장비 전력 (W)
        stats.put("totalPower", facilityPower); // 전체 전력 (W)
        stats.put("pue", String.format("%.2f", pue)); // 소수점 2자리까지만 (예: 1.50)

        return stats;
    }

    // ==========================================
    // 4. 메인 대시보드용 통계 데이터 생성 (All-in-One)
    // ==========================================
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 1. [기본] 총 장비 개수
        long totalDevices = deviceRepository.count();
        stats.put("totalDevices", totalDevices);

        // 2. [종류별] 개수 (SVR, NET, STO, UPS)
        stats.put("svrCount", deviceRepository.countByCategory_Id("SVR"));
        stats.put("netCount", deviceRepository.countByCategory_Id("NET"));
        stats.put("stoCount", deviceRepository.countByCategory_Id("STO"));
        stats.put("upsCount", deviceRepository.countByCategory_Id("UPS"));

        // 3. [상태별] ON/OFF 비율
        long onCount = deviceRepository.countByStatus("RUNNING");
        long offCount = deviceRepository.countByStatus("OFF"); // 또는 total - onCount
        stats.put("onCount", onCount);
        stats.put("offCount", offCount);

        // 4. [공간 효율] (사용중인 Unit / 전체 Rack Unit)
        long totalSpace = rackRepository.sumTotalRackUnit(); // 분모 (전체 42U * 랙개수)
        long usedSpace = deviceRepository.sumTotalUsedHeight(); // 분자 (장비 높이 합계)
        double spaceUsage = (totalSpace == 0) ? 0.0 : ((double) usedSpace / totalSpace) * 100;

        stats.put("totalSpace", totalSpace);
        stats.put("usedSpace", usedSpace);
        stats.put("emptySpace", totalSpace - usedSpace); // 빈 공간
        stats.put("spaceUsage", String.format("%.1f", spaceUsage)); // 소수점 1자리 (예: 45.2)

        // 5. [에너지] 전력량 & PUE & EMS
        long itPower = deviceRepository.sumTotalPower();
        long facilityPower = (long) (itPower * 1.5); // 시뮬레이션 (1.5배)
        double pue = (itPower == 0) ? 1.0 : (double) facilityPower / itPower;
        long emsCount = deviceRepository.countByEmsStatus("ON");

        stats.put("itPower", itPower);
        stats.put("pue", String.format("%.2f", pue));
        stats.put("emsCount", emsCount);

        return stats;
    }

    // [1] 전체 조회 메서드 수정
    public List<Device> findAllDevices(String sortOption, String sortDir) {
        Sort sort = createSort(sortOption, sortDir);
        // 기본 findAll 대신 Member 정보까지 한 번에 가져오는 새 메서드 호출
        return deviceRepository.findAllWithMember(sort);
    }

    // [2] 검색 메서드 수정
    public List<Device> searchDevices(String keyword, String sortOption, String sortDir) {
        Sort sort = createSort(sortOption, sortDir);

        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllDevices(sortOption, sortDir);
        }

        // 기존 자동 생성 메서드 대신, @Query로 만든 최적화 메서드 호출
        // 이 메서드는 신청자 아이디로도 검색이 가능하도록 설계되었습니다.
        return deviceRepository.findAllWithMemberByKeyword(keyword, sort);
    }

    // ⭐ [NEW] 총 장비 개수 조회 메서드 추가
    public long countAllDevices() {
        return deviceRepository.count();
    }

    // ==========================================
    // 5. 랙 실장도 데이터 가공
    // ==========================================
    public List<RackDetailDto> getRackViewData(Long rackId) {
        Rack rack = rackRepository.findById(rackId)
                .orElseThrow(() -> new IllegalArgumentException("없는 랙입니다."));
        int totalHeight = rack.getTotalUnit().intValue();

        RackDetailDto[] slots = new RackDetailDto[totalHeight + 1];
        for (int i = 1; i <= totalHeight; i++) {
            slots[i] = RackDetailDto.builder().unitNum(i).status("EMPTY").deviceName("").rowSpan(1).build();
        }

        List<Device> devices = deviceRepository.findByRackId(rackId);
        for (Device d : devices) {
            int start = d.getStartUnit();
            int end = start + d.getHeightUnit() - 1;
            if (end > totalHeight) continue;

            slots[end].setStatus("FULL");
            slots[end].setDeviceName(d.getVendor() + " " + d.getModelName());
            slots[end].setType(d.getCategory() != null ? d.getCategory().getId() : "ETC");
            slots[end].setRowSpan(d.getHeightUnit());
            slots[end].setDeviceId(d.getId());
            slots[end].setRunStatus(d.getStatus());
            slots[end].setSerialNum(d.getSerialNum());
            slots[end].setIpAddr(d.getIpAddr());

            // ⭐ [추가] 실장도 팝업용 데이터 매핑
            slots[end].setPowerWatt(d.getPowerWatt());
            slots[end].setEmsStatus(d.getEmsStatus());

            for (int j = start; j < end; j++) {
                slots[j].setStatus("SKIP");
                slots[j].setRunStatus(d.getStatus());
            }
        }

        List<RackDetailDto> result = new ArrayList<>();
        for (int i = totalHeight; i >= 1; i--) result.add(slots[i]);
        return result;
    }

    // ==========================================
    // 6. 삭제/수정/전원
    // ==========================================
//    @Transactional
//    public void deleteDevice(Long id) {
//        Device device = deviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("없는 장비입니다."));
//        deviceRepository.delete(device);
//        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
//        auditLogService.saveLog(currentMemberId, "장비 삭제: " + device.getSerialNum(), LogType.DEVICE_OPERATION);
//    }
    // [물리 삭제] 실제 데이터를 DB에서 삭제
    @Transactional
    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 장비입니다."));

        deviceRepository.delete(device); // 실제 삭제

        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.saveLog(currentMemberId, "장비 영구 삭제: " + device.getSerialNum(), LogType.DEVICE_OPERATION);
    }
    // [논리 삭제] 상태만 바꾸고 기록 보존
    @Transactional
    public void deleteDeviceWithReason(Long id, String reason) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 장비가 존재하지 않습니다."));

        device.setStatus("DELETED");
        device.setDeleteReason(reason);
        device.setRack(null);      // 랙 공간 반납
        device.setStartUnit(0); // 유닛 위치 반납

        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.saveLog(currentMemberId,
                "장비 논리 삭제: " + device.getSerialNum() + " (사유: " + reason + ")",
                LogType.DEVICE_OPERATION);
    }

    @Transactional
    public void updateDevice(Long id, Device formDevice, Long rackId, String cateId) { // rackId 파라미터 추가 추천
        // 1. 공간 점유 체크
        checkRackOverlap(rackId, formDevice.getStartUnit(), formDevice.getHeightUnit(), id);

        Device target = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장비가 없습니다."));

        // 2. 랙 정보 업데이트
        if (rackId != null) {
            Rack newRack = rackRepository.findById(rackId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 랙입니다."));
            target.setRack(newRack);
        }

        // 3. ⭐ [추가] 카테고리 정보 업데이트
        if (cateId != null) {
            Category newCategory = categoryRepository.findById(cateId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
            target.setCategory(newCategory);
        }

        // 4. 위치 및 나머지 정보 업데이트 (기존 코드)
        target.setStartUnit(formDevice.getStartUnit());
        target.setHeightUnit(formDevice.getHeightUnit());

        // 5. 나머지 정보 업데이트 (기존 코드 유지)
        target.setVendor(formDevice.getVendor());
        target.setModelName(formDevice.getModelName());
        target.setSerialNum(formDevice.getSerialNum());
        target.setIpAddr(formDevice.getIpAddr());
        target.setPowerWatt(formDevice.getPowerWatt());
        target.setEmsStatus(formDevice.getEmsStatus());
        target.setContractMonth(formDevice.getContractMonth());
        target.setContractDate(formDevice.getContractDate());
        target.setCompanyName(formDevice.getCompanyName());
        target.setCompanyPhone(formDevice.getCompanyPhone());
        target.setUserName(formDevice.getUserName());
        target.setContact(formDevice.getContact());
        target.setDescription(formDevice.getDescription());

        // 로그 기록
        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.saveLog(currentMemberId, "장비 정보 및 위치 수정: " + target.getSerialNum(), LogType.DEVICE_OPERATION);
    }

    @Transactional
    public String toggleStatus(Long id) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("장비가 없습니다."));
        if ("RUNNING".equals(device.getStatus())) device.setStatus("OFF");
        else device.setStatus("RUNNING");

        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.saveLog(currentMemberId, "전원 변경(" + device.getStatus() + "): " + device.getSerialNum(), LogType.DEVICE_OPERATION);
        return device.getStatus();
    }

    public Device findById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 장비가 존재하지 않습니다."));
    }

    // ==========================================
    // 7. 중복 검사 로직
    // ==========================================
    public boolean isSerialDuplicate(String serialNum, Long currentId) {
        if (currentId == null) {
        // 신규 등록일 때: 전체에서 중복 확인
        return deviceRepository.existsBySerialNum(serialNum);
        } else {
        // 수정 시: 나(currentId)를 제외한 나머지 중에서 동일한 시리얼이 있는지 확인
        return deviceRepository.existsBySerialNumAndIdNot(serialNum, currentId);
        }
    }

    // ==========================================
    // 8. 랙 공간 점유 체크 로직 (분리)
    // ==========================================
    public void checkRackOverlap(Long rackId, Integer startUnit, Integer heightUnit, Long currentDeviceId) {
        Rack rack = rackRepository.findById(rackId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 랙입니다."));

        int newEnd = startUnit + heightUnit - 1;

        // 랙 높이 초과 검사
        if (newEnd > rack.getTotalUnit()) {
            throw new IllegalStateException("장비 설치 위치가 랙의 최대 높이(" + rack.getTotalUnit() + "U)를 벗어납니다.");
        }

        // 해당 랙의 모든 장비 조회
        List<Device> existingDevices = deviceRepository.findByRackId(rackId);
        for (Device existing : existingDevices) {
            // 수정 시: 자기 자신과의 충돌은 무시
            if (currentDeviceId != null && existing.getId().equals(currentDeviceId)) {
                continue;
            }

            int exStart = existing.getStartUnit();
            int exEnd = exStart + existing.getHeightUnit() - 1;

            // 충돌 판정 공식
            if (startUnit <= exEnd && newEnd >= exStart) {
                throw new IllegalStateException("이미 해당 위치(" + exStart + "~" + exEnd + "U)에 다른 장비가 있습니다.");
            }
        }
    }

    // ==========================================
    // 9. 장비 추가 시 환경 정보 업데이트
    // ==========================================
    @Transactional
    public void addDevice(Device device) {
        deviceRepository.save(device);

        // ⭐ [추가] 장비가 늘었으니 전력량 및 PUE 다시 계산
        envService.calculateSimulation(null);
    }

    // 일괄처리 기능
    @Transactional
    public void updateMultipleDevices(deviceDTO dto) {
        // 1. 대상 장비들을 한 번의 쿼리로 조회
        List<Device> devices = deviceRepository.findAllById(dto.getIds());

        if (devices.isEmpty()) return;

        // 2. 루프를 돌며 변경 사항 적용
        for (Device device : devices) {
            // EMS 상태 변경 (값이 있을 때만)
            if (dto.getEmsStatus() != null && !dto.getEmsStatus().trim().isEmpty()) {
                device.setEmsStatus(dto.getEmsStatus());
            }

            // 장비 상태 변경 (값이 있을 때만)
            if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
                device.setStatus(dto.getStatus());
            }
        }
        // @Transactional 어노테이션 덕분에 메서드 종료 시
        // Dirty Checking(변경 감지)이 일어나 DB에 자동 반영됩니다.
    }

//    @Transactional
//    public void deleteMultipleDevices(List<Long> ids) {
//        if (ids == null || ids.isEmpty()) return;
//
//        // Batch 삭제를 사용하여 성능 최적화 (한 번의 쿼리로 삭제)
//        deviceRepository.deleteAllByIdInBatch(ids);
//    }
        @Transactional
        public void deleteMultipleDevices(List<Long> ids) {
            if (ids == null || ids.isEmpty()) return;

            // 로그 기록을 위해 삭제 전 대상 정보 가져오기 (선택사항)
            int count = ids.size();

            deviceRepository.deleteAllByIdInBatch(ids); // 영구 삭제

            String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
            auditLogService.saveLog(currentMemberId, "장비 " + count + "건 일괄 영구 삭제", LogType.DEVICE_OPERATION);
        }

    // [사용자용] 페이징 처리된 내 장비 목록
    public Page<Device> getMyDeviceList(String memberId, String keyword, Pageable pageable) {
        // Repository에 위에서 제안한 findMyDevicesByKeyword 등의 메서드가 있어야 함
        if (keyword == null || keyword.trim().isEmpty()) {
            return deviceRepository.findMyDevices(memberId, pageable);
        }
        return deviceRepository.findMyDevicesByKeyword(memberId, keyword, pageable);
    }

    // [추가] 마감일 만료 체크 로직 (필요 시 컨트롤러나 스케줄러에서 호출)
    // 실제로는 Device 엔티티에 getEndDate() 메서드가 (contractDate + contractMonth)로 구현되어 있어야 합니다.
    public boolean isDeviceExpired(Device device) {
        if (device.getContractDate() == null) return false;
        // contractDate(시작일)에 contractMonth(기간)을 더한 날짜와 오늘 비교
        return device.getContractDate().plusMonths(device.getContractMonth()).isBefore(LocalDate.now());
    }
}