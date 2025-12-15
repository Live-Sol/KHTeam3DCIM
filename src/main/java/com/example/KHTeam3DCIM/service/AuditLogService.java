package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.AuditLog;
import com.example.KHTeam3DCIM.domain.LogType;
import com.example.KHTeam3DCIM.repository.AuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // 기본은 읽기 전용 (속도 향상)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // 관리자 대시보드용 로그 조회
    public List<AuditLog> getRecentActivityLogs(int limit) {
        return auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, limit));
    }

    public List<AuditLog> findRecentLogs(int limit){
        return auditLogRepository.findTopByOrderByTimestampDesc(limit);
    }

    // 통계용 (더미 데이터 -> 실제 Repository 연결 권장)
    public int getPendingRequestCount() { return 5; }
    public int getTotalDeviceCount() { return 120; }
    public int getTotalMemberCount() { return 45; }

    // 🚨 [수술 부위] 쓰기 전용 트랜잭션 추가!
    @Transactional
    public void saveLog(String actor, String actionDescription, LogType type) {
        AuditLog log = new AuditLog(actor, actionDescription, type);
        auditLogRepository.save(log);
    }
}