// RequestRepository.java
// 파일의 역할 : 신청서(Request)를 데이터베이스에 저장하고 조회하는 역할을 합니다.

package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.domain.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    // 1. 상태별로 조회 (관리자용)
    List<Request> findByStatusOrderByReqDateDesc(String status);
    // 2. 상태별 '개수' 세기 (대시보드용)
    long countByStatus(String status);

    // 3. 특정 사용자(memberId)가 신청한 내역을 최신순으로 조회 (이용자 이력용)
    // 💡 주의: Request 엔티티에 private String memberId; 필드가 있어야 합니다.
    List<Request> findByMemberIdOrderByReqDateDesc(String memberId);

    //  4. 검색 및 필터 쿼리 (JPQL)
    // 1. status는 무조건 'WAITING' (대기 목록이니까)
    // 2. keyword가 비어있으면 무시, 있으면 회사명 or 담당자명에서 검색 (LIKE 검색)
    // 3. emsStatus가 비어있으면 무시, 있으면 해당 상태만 필터링
    @Query("SELECT r FROM Request r " +
            "WHERE r.status = 'WAITING' " +
            "AND (:keyword IS NULL OR r.companyName LIKE %:keyword% OR r.userName LIKE %:keyword%) " +
            "AND (:emsStatus IS NULL OR r.emsStatus = :emsStatus) " +
            "ORDER BY r.reqDate DESC")
    List<Request> searchWaitingRequests(@Param("keyword") String keyword,
                                        @Param("emsStatus") String emsStatus);


}

