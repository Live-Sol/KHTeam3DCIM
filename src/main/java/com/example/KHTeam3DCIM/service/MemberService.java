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
    public Member findByMemberIdOrThrow(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
    }

    // 3. 회원 등록
    public Member addMember(Member member) {
        // 필요시 비밀번호 암호화 등 처리
        return memberRepository.save(member);
    }

    // 4. 회원 정보 수정
    @Transactional
    public Member updateMember(String memberId, Member patch) {
        return memberRepository.findById(memberId)
                .map(existing ->{
                    if(patch.getPassword() != null)
                        existing.setPassword(patch.getPassword());
                    if(patch.getName() != null)
                        existing.setName(patch.getName());
                    return memberRepository.save(existing);
                })
                .orElse(null);
    }

    // 5. 회원 삭제
    public void deleteMember(String memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new RuntimeException("회원이 존재하지 않습니다.");
        }
        memberRepository.deleteById(memberId);
    }

}
