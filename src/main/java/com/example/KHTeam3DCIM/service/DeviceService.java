package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.*;
import com.example.KHTeam3DCIM.dto.Rack.RackDetailDto;
import com.example.KHTeam3DCIM.repository.CategoryRepository;
import com.example.KHTeam3DCIM.repository.DeviceRepository;
import com.example.KHTeam3DCIM.repository.RackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // ==========================================
    // 1. 장비 등록하기
    // ==========================================
    @Transactional
    public Long registerDevice(Long rackId, String cateId, Device newDevice) {
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

    public List<Device> findAllDevices(String sortOption, String sortDir) {
        Sort sort = createSort(sortOption, sortDir);
        return deviceRepository.findAll(sort);
    }

    public List<Device> searchDevices(String keyword, String sortOption, String sortDir) {
        Sort sort = createSort(sortOption, sortDir);
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllDevices(sortOption, sortDir);
        }
        return deviceRepository.findByVendorContainingIgnoreCaseOrModelNameContainingIgnoreCaseOrSerialNumContainingIgnoreCase(
                keyword, keyword, keyword, sort);
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
    @Transactional
    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("없는 장비입니다."));
        deviceRepository.delete(device);
        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.saveLog(currentMemberId, "장비 삭제: " + device.getSerialNum(), LogType.DEVICE_OPERATION);
    }

    @Transactional
    public void updateDevice(Long id, Device formDevice) {
        Device target = deviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("장비가 없습니다."));

        // 🚑 [수정] 충돌 검사 로직이 필요하다면 여기에 추가해야 함 (현재는 생략)

        target.setVendor(formDevice.getVendor());
        target.setModelName(formDevice.getModelName());
        target.setSerialNum(formDevice.getSerialNum());
        target.setIpAddr(formDevice.getIpAddr());

        // ⭐ [수정] 여기가 빠져있어서 수정이 안 됐습니다!
        target.setPowerWatt(formDevice.getPowerWatt());
        target.setEmsStatus(formDevice.getEmsStatus());
        target.setContractMonth(formDevice.getContractMonth());
        target.setContractDate(formDevice.getContractDate());
        target.setCompanyName(formDevice.getCompanyName());
        target.setCompanyPhone(formDevice.getCompanyPhone());
        target.setUserName(formDevice.getUserName());
        target.setContact(formDevice.getContact());
        target.setDescription(formDevice.getDescription());

        // 위치 변경은 현재 미지원 (복잡도 때문)

        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.saveLog(currentMemberId, "장비 정보 수정: " + target.getSerialNum(), LogType.DEVICE_OPERATION);
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
}