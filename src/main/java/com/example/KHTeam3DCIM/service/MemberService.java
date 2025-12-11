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
import java.util.regex.Pattern;
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

    // 회원 아이디로 회원 조회
    public List<MemberResponse> findMemberByIdOrLike(String memberId) {
        List<Member> members = memberRepository.findByMemberIdLike("%" + memberId + "%");  // 부분 일치 검색
        if (members.isEmpty()) {
            throw new RuntimeException("회원이 존재하지 않습니다.");  // 예외 처리
        }
        return members.stream()
                .map(m -> MemberResponse.builder()
                        .name(m.getName())
                        .role(m.getRole())
                        .build())
                .collect(Collectors.toList());
    }


    // 회원 등록
    public MemberResponse addMember(MemberCreateRequest request) {
        // 아이디 유효성 검사
        String memberId = request.getMemberId();
        if (!Pattern.matches("^[a-z0-9]{4,20}$", memberId)) {
            throw new RuntimeException("아이디는 알파벳 소문자와 숫자만 가능하며, 4~20글자여야 합니다.");
        }
        // 아이디 중복 검사
        if (memberRepository.existsByMemberId(memberId)) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        // 비밀번호 유효성 검사
        String password = request.getPassword();
        if (password.length() < 4 || password.length() > 20) {
            throw new RuntimeException("비밀번호는 4~20글자 사이여야 합니다.");
        }

        // 이름 유효성 검사
        String name = request.getName();
        if (!Pattern.matches("^[a-zA-Z가-힣]{2,10}$", name)) {
            throw new RuntimeException("이름은 한글과 알파벳만 가능하며, 2~10글자여야 합니다.");
        }

        // 회원 객체 생성 및 저장
        Member member = Member.builder()
                .memberId(memberId)
                .password(password)  // 필요시 암호화
                .name(name)
                .role(Role.USER)  // 기본값 USER로 설정
                .build();

        Member saved = memberRepository.save(member);

        // 회원 응답 반환
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
