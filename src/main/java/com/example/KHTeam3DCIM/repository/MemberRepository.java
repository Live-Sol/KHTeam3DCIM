package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,String> {
//    Optional<Member> findByName(String name);   // 이름으로 멤버 조회
//    Optional<Member> findByRole(Member.Role role);  // 역할로 멤버 조회
}
