package com.example.KHTeam3DCIM.dto.device;

import com.example.KHTeam3DCIM.domain.Device;

import java.time.LocalDate;

/**
 * 사용자용 장비 상세 정보 응답 DTO
 * 보안을 위해 위치(Rack), 유닛(Unit), IP 주소 등은 제외함
 */
public record DeviceResponse(
        String serialNum,
        String vendor,
        String modelName,
        Integer heightUnit,
        LocalDate contractDate,
        Integer contractMonth,
        String status,
        String deleteReason,
        // 추가 필드
        Integer powerWatt, // 소비전력
        String emsStatus,         // EMS 유무 (ON/OFF)
        String description        // 용도 및 설명
) {
    public static DeviceResponse from(Device device) {
        return new DeviceResponse(
                device.getSerialNum(),
                device.getVendor(),
                device.getModelName(),
                device.getHeightUnit(),
                device.getContractDate(),
                device.getContractMonth(),
                device.getStatus(),
                device.getDeleteReason(),
                // 매핑 추가
                device.getPowerWatt(),
                device.getEmsStatus(),
                device.getDescription()
        );
    }
}