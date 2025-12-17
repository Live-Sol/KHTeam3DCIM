package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,String> {
    // 회원 아이디로 부분 일치 검색 (LIKE 쿼리)
    List<Member> findByMemberIdLike(String memberId);
    // 아이디 중복 여부 확인
    boolean existsByMemberId(String memberId);  // 자동으로 구현됩니다.
    // 회원 ID 조회
    Optional<Member> findByMemberId(String memberId);
    // 전체 회원을 Role을 기준으로 오름차순(Asc) 정렬하여 조회
    List<Member> findAllByOrderByRoleAsc();

}
