package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.*;
import com.example.KHTeam3DCIM.dto.Rack.RackDetailDto;
import com.example.KHTeam3DCIM.repository.CategoryRepository;
import com.example.KHTeam3DCIM.repository.DeviceRepository;
import com.example.KHTeam3DCIM.repository.RackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder; // ⭐️ 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

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

        // 통과했으면 관계 맺어주기
        newDevice.setRack(rack);
        newDevice.setCategory(category);

        // 상태가 없으면 기본값 'OFF'로 설정
        if (newDevice.getStatus() == null || newDevice.getStatus().isEmpty()) {
            newDevice.setStatus("OFF");
        }

        // 장비 저장 (DB에 INSERT)
        deviceRepository.save(newDevice);

        // ⭐️ AuditLog 저장 (팀원 코드 활용)
        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName(); // 로그인한 ID 가져오기
        auditLogService.saveLog(currentMemberId, "장비 등록: " + newDevice.getSerialNum(), LogType.DEVICE_OPERATION);

        return newDevice.getId();
    }

    // ==========================================
    // 2. 조회 기능들
    // ==========================================
// [1] 수정: 정렬 옵션(무엇을)과 방향(어떻게)을 모두 받아서 처리
    private Sort createSort(String sortOption, String sortDir) {

        // 1. 방향 결정 (기본값은 DESC)
        // 화면에서 "asc"라고 보내면 오름차순(ASC), 아니면 내림차순(DESC)
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // 2. 정렬할 속성(필드명) 결정
        String property = switch (sortOption) {
            case "id_asc" -> "id"; // ID 기준
            case "rack" -> "rack.rackName"; // 랙 이름 기준
            case "category" -> "category.name"; // 카테고리 이름 기준
            case "serial" -> "serialNum"; // 시리얼 번호 기준
            case "location" -> "startUnit"; // 위치(Unit) 기준
            case "status" -> "status"; // 상태 기준
            default -> "id"; // 기본값(latest 등)은 ID 기준
        };

        // 3. Sort 객체 생성 (방향 + 속성)
        return Sort.by(direction, property);
    }

    // [2] 전체 조회 (파라미터 추가)
    public List<Device> findAllDevices(String sortOption, String sortDir) {
        Sort sort = createSort(sortOption, sortDir);
        return deviceRepository.findAll(sort);
    }

    // [3] 검색 조회 (파라미터 추가)
    public List<Device> searchDevices(String keyword, String sortOption, String sortDir) {
        Sort sort = createSort(sortOption, sortDir);
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllDevices(sortOption, sortDir);
        }
        return deviceRepository.findByVendorContainingIgnoreCaseOrModelNameContainingIgnoreCaseOrSerialNumContainingIgnoreCase(
                keyword, keyword, keyword, sort);
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
    // 4. 삭제/수정/전원
    // ==========================================
    @Transactional
    public void deleteDevice(Long id) { // 장비 삭제
        Device device = deviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("없는 장비입니다."));
        deviceRepository.delete(device);

        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.saveLog(currentMemberId, "장비 삭제: " + device.getSerialNum(), LogType.DEVICE_OPERATION);
    }

    @Transactional
    public void updateDevice(Long id, Device formDevice) { // 장비 수정
        Device target = deviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("장비가 없습니다."));
        target.setVendor(formDevice.getVendor());
        target.setModelName(formDevice.getModelName());
        target.setSerialNum(formDevice.getSerialNum());
        target.setIpAddr(formDevice.getIpAddr());

        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.saveLog(currentMemberId, "장비 정보 수정: " + target.getSerialNum(), LogType.DEVICE_OPERATION);
    }

    @Transactional
    public String toggleStatus(Long id) { // 전원 토글
        Device device = deviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("장비가 없습니다."));
        if ("RUNNING".equals(device.getStatus())) device.setStatus("OFF");
        else device.setStatus("RUNNING");

        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.saveLog(currentMemberId, "전원 변경(" + device.getStatus() + "): " + device.getSerialNum(), LogType.DEVICE_OPERATION);
        return device.getStatus();
    }

    // ==========================================
    // [추가] 단건 조회 (Controller에서 호출함)
    // ==========================================
    public Device findById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 장비가 존재하지 않습니다."));
    }
}