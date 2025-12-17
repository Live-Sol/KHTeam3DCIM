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


}
