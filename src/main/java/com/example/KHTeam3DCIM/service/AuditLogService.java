package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.AuditLog; // AuditLog의 실제 패키지 경로로 수정 필요
import com.example.KHTeam3DCIM.domain.LogType;
import com.example.KHTeam3DCIM.repository.AuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // 데이터 조회만 하므로 readOnly=true로 설정
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    // 나중에 MemberRepository, DeviceRepository 등을 추가합니다.

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * 관리자 대시보드에 표시할 최신 활동 로그 목록을 가져옵니다.
     * @param limit 가져올 로그의 최대 개수
     * @return 최신 활동 로그 목록
     */
    public List<AuditLog> getRecentActivityLogs(int limit) {
        // PageRequest.of(페이지 번호 0, 개수 limit)를 사용하여 최신 N개만 가져옵니다.
        return auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, limit));
    }

    // --- 통계 데이터 (현재는 더미, 나중에 Repository 연동 필요) ---
    public int getPendingRequestCount() {
        // TODO: 실제 RequestRepository를 사용하여 countByStatus("PENDING") 로직 구현
        return 5;
    }

    public int getTotalDeviceCount() {
        // TODO: 실제 DeviceRepository를 사용하여 count() 로직 구현
        return 120;
    }

    public int getTotalMemberCount() {
        // TODO: 실제 MemberRepository를 사용하여 count() 로직 구현
        return 45;
    }

    // AuditLogService.java - saveLog 메서드 수정
    public void saveLog(String actor, String actionDescription, LogType type) {
        // ⭐️ 인자가 3개인 현재 AuditLog 생성자를 사용하도록 수정 ⭐️
        AuditLog log = new AuditLog(actor, actionDescription, type);
        // timestamp는 @CreatedDate가 자동 주입합니다.
        auditLogRepository.save(log);
    }

    // 지정된 개수만큼의 최근 활동 로그를 조회합니다.
    public List<AuditLog> findRecentLogs(int limit){
        return auditLogRepository.findTopByOrderByTimestampDesc(limit);
    }

}