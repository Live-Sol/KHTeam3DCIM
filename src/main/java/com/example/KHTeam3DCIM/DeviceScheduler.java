package com.example.KHTeam3DCIM;

import com.example.KHTeam3DCIM.domain.Device;
import com.example.KHTeam3DCIM.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeviceScheduler {

    private final DeviceRepository deviceRepository;

    // 매일 새벽 0시 1분에 계약 만료 장비 체크
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void checkExpiredDevices() {
        LocalDate today = LocalDate.now();
        // 전체가 아닌 활동 중인 장비만 필터링해서 조회
        List<Device> activeDevices = deviceRepository.findAllActiveDevices();

        for (Device device : activeDevices) {
            LocalDate endDate = device.getEndDate();
            if (endDate != null && endDate.isBefore(today)) {
                device.setStatus("DELETED");
                device.setDeleteReason("계약 기간 만료로 인한 자동 시스템 삭제");
                device.setRack(null);
                // 별도의 save 호출 없이 @Transactional에 의해 자동 반영(Dirty Checking)됩니다.
            }
        }
    }
}