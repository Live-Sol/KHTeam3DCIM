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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final RackRepository rackRepository;
    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    // ... (기존 registerDevice, createSort 등 메서드는 유지) ...
    // ... (전체 파일을 다시 덮어쓰셔도 됩니다. 아래는 기존 코드 포함 전체입니다.) ...

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
    // 3. 랙 실장도 데이터 가공
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
    // 4. 삭제/수정/전원
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