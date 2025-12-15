package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.AuditLog;
import com.example.KHTeam3DCIM.domain.LogType;
import com.example.KHTeam3DCIM.repository.AuditLogRepository;
import com.example.KHTeam3DCIM.repository.DeviceRepository;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import com.example.KHTeam3DCIM.repository.RequestRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // 기본은 읽기 전용 (속도 향상)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final MemberRepository memberRepository;
    private final DeviceRepository deviceRepository;
    private final RequestRepository requestRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, MemberRepository memberRepository,
                           DeviceRepository deviceRepository, RequestRepository requestRepository) {
        this.auditLogRepository = auditLogRepository;
        this.memberRepository = memberRepository;
        this.deviceRepository = deviceRepository;
        this.requestRepository = requestRepository;
    }

    // 관리자 대시보드용 로그 조회
    public List<AuditLog> getRecentActivityLogs(int limit) {
        return auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, limit));
    }

    public List<AuditLog> findRecentLogs(int limit){
        return auditLogRepository.findTopByOrderByTimestampDesc(limit);
    }

    // 통계용 (더미 데이터 -> 실제 Repository 연결 권장)
    public int getPendingRequestCount() {
        return (int) requestRepository.count(); }
    public int getTotalDeviceCount() {
        return (int) deviceRepository.count(); }
    public int getTotalMemberCount() {
        return (int) memberRepository.count(); }

    // 🚨 [수술 부위] 쓰기 전용 트랜잭션 추가!
    @Transactional
    public void saveLog(String actor, String actionDescription, LogType type) {
        AuditLog log = new AuditLog(actor, actionDescription, type);
        auditLogRepository.save(log);
    }
}