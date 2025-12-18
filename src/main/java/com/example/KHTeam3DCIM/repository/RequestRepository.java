// RequestRepository.java
// 파일의 역할 : 신청서(Request)를 데이터베이스에 저장하고 조회하는 역할을 합니다.

package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.domain.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    // 1. 상태별로 조회 (관리자용)
    List<Request> findByStatusOrderByReqDateDesc(String status);

    // 2. 상태별 '개수' 세기 (대시보드용)
    long countByStatus(String status);

    // 3. [추가] 특정 사용자(memberId)가 신청한 내역을 최신순으로 조회 (이용자 이력용)
    // 💡 주의: Request 엔티티에 private String memberId; 필드가 있어야 합니다.
    List<Request> findByMemberIdOrderByReqDateDesc(String memberId);
}
