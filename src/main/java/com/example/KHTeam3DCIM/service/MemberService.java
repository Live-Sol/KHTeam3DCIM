package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    // 1. 전체 회원 조회
    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    // 2. 회원 아이디로 조회
    public Optional<Member> findByMemberId(String memberId) {
        return memberRepository.findById(memberId);
    }

    // 3. 회원 등록
    public Member saveMember(Member member) {
        // 필요시 비밀번호 암호화 등 처리
        return memberRepository.save(member);
    }

    // 4. 회원 삭제
    public void deleteMember(String memberId) {
        memberRepository.deleteById(memberId);
    }
}
