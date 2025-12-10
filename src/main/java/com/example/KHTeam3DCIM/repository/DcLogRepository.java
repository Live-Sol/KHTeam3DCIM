// DcLogRepository.java
// 로그를 저장하는 심부름꾼입니다.

package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.domain.DcLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DcLogRepository extends JpaRepository<DcLog, Long> {

    // 최근 로그 5개만 가져옵니다. (메인화면에 출력하기위해)
    // Top5: 5개만
    // ByOrderByLogDateDesc: logDate 기준으로 내림차순 정렬
    List<DcLog> findTop5ByOrderByLogDateDesc();
}
