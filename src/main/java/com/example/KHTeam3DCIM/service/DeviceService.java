package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.*;
import com.example.KHTeam3DCIM.dto.Rack.RackDetailDto;
import com.example.KHTeam3DCIM.repository.CategoryRepository;
import com.example.KHTeam3DCIM.repository.DeviceRepository;
import com.example.KHTeam3DCIM.repository.RackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder; // ⭐️ 추가
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
    private final AuditLogService auditLogService; // ⭐️ DcLogRepository 대신 이거 사용!

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
        deviceRepository.save(newDevice);

        // ⭐️ AuditLog 저장 (팀원 코드 활용)
        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName(); // 로그인한 ID 가져오기
        auditLogService.saveLog(currentMemberId, "장비 등록: " + newDevice.getSerialNum(), LogType.DEVICE_OPERATION);

        return newDevice.getId();
    }

    // ==========================================
    // 2. 조회 기능들
    // ==========================================
    public List<Device> findAllDevices() { return deviceRepository.findAll(); }
    public List<Device> findDevicesByRack(Long rackId) { return deviceRepository.findByRackId(rackId); }
    public Device findBySerial(String serialNum) { return deviceRepository.findBySerialNum(serialNum).orElse(null); }
    public Device findById(Long id) { return deviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("없음")); }

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
            slots[end].setSerialNum(d.getSerialNum()); // 툴팁용
            slots[end].setIpAddr(d.getIpAddr());       // 툴팁용

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
    // 4. 검색/삭제/수정/전원
    // ==========================================
    public List<Device> searchDevices(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return findAllDevices();
        return deviceRepository.findByVendorContainingIgnoreCaseOrModelNameContainingIgnoreCaseOrSerialNumContainingIgnoreCase(keyword, keyword, keyword);
    }

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
        target.setVendor(formDevice.getVendor());
        target.setModelName(formDevice.getModelName());
        target.setSerialNum(formDevice.getSerialNum());
        target.setIpAddr(formDevice.getIpAddr());

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
}