package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.domain.Role;
import com.example.KHTeam3DCIM.dto.Member.*;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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

    // 회원 단건 조회
    public Member findMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
    }

    // 회원 등록
    public MemberResponse addMember(MemberCreateRequest request) {

        // 아이디 유효성 검사
        String memberId = request.getMemberId();

        // 아이디 중복 검사
        if (memberRepository.existsByMemberId(memberId)) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        // 비밀번호 유효성 검사
        String rawPassword = request.getPassword();
        // 비밀번호 암호화 적용
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 이름 유효성 검사
        String name = request.getName();

        // 회원 객체 생성 및 저장
        Member member = Member.builder()
                .memberId(memberId)
                .password(encodedPassword)  // 암호화된 비밀번호
                .name(name)
                .email(request.getEmail())
                .contact(request.getContact())
                .role(Role.USER)  // 기본값 USER로 설정
                .build();

        Member saved = memberRepository.save(member);

        // 회원 응답 반환
        return MemberResponse.builder()
                .name(saved.getName())
                .role(saved.getRole())
                .build();
    }

    // 회원 정보 수정(본인)
    @Transactional
    public MemberResponse updateMember(String memberId, MemberUpdateRequest patch) {
        Member updated = memberRepository.findById(memberId)
                // .map(existing -> {...}) 내부가 람다 스코프입니다.
                .map(existing -> {
                    // 1. 비밀번호 업데이트 (변경 요청이 있을 경우에만)
                    if (patch.getPassword() != null && !patch.getPassword().isEmpty()) { // 비밀번호 필드 비어있지 않은지 추가 확인
                        String encodedNewPassword = passwordEncoder.encode(patch.getPassword());
                        existing.setPassword(encodedNewPassword);
                    }

                    // 2. 이름 업데이트
                    if (patch.getName() != null)
                        existing.setName(patch.getName());

                    // ⭐️ 3. 이메일 업데이트 (추가) ⭐️
                    if (patch.getEmail() != null)
                        existing.setEmail(patch.getEmail());

                    // ⭐️ 4. 연락처 업데이트 (추가) ⭐️
                    if (patch.getContact() != null)
                        existing.setContact(patch.getContact());

                    // 별도의 save 호출 없이 @Transactional에 의해 변경 사항이 DB에 반영됩니다.
                    return existing;
                })
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
        return MemberResponse.builder()
                .name(updated.getName())
                .role(updated.getRole())
                .build();
    }

    // 회원 삭제 (회원 본인)
    public void deleteMemberWithPassword(String memberId, String password) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        memberRepository.delete(member);
    }
}