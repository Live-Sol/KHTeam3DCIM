// RequestRepository.java
// 파일의 역할 : 신청서(Request)를 데이터베이스에 저장하고 조회하는 역할을 합니다.

package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.domain.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    // 1. 상태별로 조회 (WAITING 인 것만 관리자가 봐야 하기 때문!!)
    List<Request> findByStatusOrderByReqDateDesc(String status);

    // 2. 상태별 '개수' 세기 (대시보드 숫자용)
    // SQL: SELECT COUNT(*) FROM DC_REQUEST WHERE STATUS = ?
    long countByStatus(String status);
}
