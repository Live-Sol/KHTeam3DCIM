package com.example.KHTeam3DCIM;

import com.example.KHTeam3DCIM.domain.Device;
import com.example.KHTeam3DCIM.domain.LogType;
import com.example.KHTeam3DCIM.repository.DeviceRepository;
import com.example.KHTeam3DCIM.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceScheduler {

    private final DeviceRepository deviceRepository;
    private final AuditLogService auditLogService;



    // 매일 새벽 3시에 실행 (Cron 표현식: 초 분 시 일 월 요일)
    // @Scheduled(cron = "0 0 3 * * *")
    // 10초마다 실행 <-- 이렇게 변경하세요
    @Scheduled(cron = "0/10 * * * * *")
    @Transactional
    public void cleanupDeletedDevices() {
        // 30일 이전 날짜 계산
        // LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        // [수정 2] 기준 시간 변경: 30일 전 -> "1분 전"
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(60);

        // 논리 삭제 상태이면서 삭제된 지 30일(혹은 1분)이 지난 장비 조회
        List<Device> expiredDevices = deviceRepository.findByStatusAndDeletedAtBefore("DELETED", threshold);

        if (!expiredDevices.isEmpty()) {
            deviceRepository.deleteAll(expiredDevices);
            log.info("{} 건의 오래된 장비 데이터를 물리 삭제했습니다.", expiredDevices.size());

            // 시스템 기록 (필요 시)
            auditLogService.saveLog("SYSTEM",
                    expiredDevices.size() + " 건 장비 자동 영구 삭제 완료",
                    LogType.DEVICE_OPERATION);
        }
    }
}