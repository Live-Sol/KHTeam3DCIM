package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.domain.Role;
import com.example.KHTeam3DCIM.dto.Member.MemberAdminResponse;
import com.example.KHTeam3DCIM.dto.Member.MemberCreateRequest;
import com.example.KHTeam3DCIM.dto.Member.MemberResponse;
import com.example.KHTeam3DCIM.dto.Member.MemberUpdateRequest;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    // 전체 회원 조회 (회원용 - 이름, role 표기)
    public List<MemberResponse> findAllMembersUser() {
        return memberRepository.findAll()
                .stream()
                .map(m -> MemberResponse.builder()
                        .name(m.getName())
                        .role(m.getRole())
                        .build())
                .collect(Collectors.toList());
    }
    // 전체 회원 조회 (관리자용 - id, 이름, role 표기)
    public List<MemberAdminResponse> findAllMembersAdmin() {
        return memberRepository.findAll()
                .stream()
                .map(m -> MemberAdminResponse.builder()
                        .memberId(m.getMemberId())
                        .name(m.getName())
                        .role(m.getRole())
                        .build())
                .collect(Collectors.toList());
    }

    // 회원 아이디로 조회
    public MemberResponse findMemberById(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
        return MemberResponse.builder()
                .name(member.getName())
                .role(member.getRole())
                .build();

    }

    // 회원 등록
    public MemberResponse addMember(MemberCreateRequest request) {
        Member member = Member.builder()
                .memberId(request.getMemberId())
                .password(request.getPassword())    // 필요시 암호화
                .name(request.getName())
                .role(Role.USER)    // 가입시 기본값 USER로 가입
                .build();
        Member saved = memberRepository.save(member);
        return MemberResponse.builder()
                .name(saved.getName())
                .role(saved.getRole())
                .build();
    }

    // 회원 정보 수정
    @Transactional
    public MemberResponse updateMember(String memberId, MemberUpdateRequest patch) {
        Member updated = memberRepository.findById(memberId)
                .map(existing ->{
                    if(patch.getPassword() != null)
                        existing.setPassword(patch.getPassword());
                    if(patch.getName() != null)
                        existing.setName(patch.getName());
                    return memberRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
        return MemberResponse.builder()
                .name(updated.getName())
                .role(updated.getRole())
                .build();
    }

    // 회원 삭제
    public void deleteMember(String memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new RuntimeException("회원이 존재하지 않습니다.");
        }
        memberRepository.deleteById(memberId);
    }

}
