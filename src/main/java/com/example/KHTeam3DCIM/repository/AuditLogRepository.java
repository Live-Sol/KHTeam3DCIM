package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.entity.AuditLog; // AuditLog의 실제 패키지 경로로 수정 필요
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// JpaRepository를 상속받아 기본적인 CRUD 기능을 자동으로 제공받습니다.
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * 관리자 페이지에 표시할 최신 로그 N개를 시간(timestamp) 내림차순으로 조회합니다.
     * JPA Query Method 명명 규칙을 사용하여 복잡한 쿼리 없이 필요한 메서드를 정의합니다.
     * @param pageable 조회할 개수 및 페이지 정보 (PageRequest.of(0, N))
     * @return 최신 활동 로그 목록
     */
    List<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
    //최근 로그 기록을 내림차순으로 지정된 수만큼 조회합니다.
    //List<AuditLog> findRecentLogs(int limit);

    List<AuditLog> findTopByOrderByTimestampDesc(int limit);
}