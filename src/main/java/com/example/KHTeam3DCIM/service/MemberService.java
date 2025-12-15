package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.LogType;
import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.domain.Role;
import com.example.KHTeam3DCIM.dto.Member.*;
import com.example.KHTeam3DCIM.dto.admin.MemberAdminUpdateRequest;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final AuditLogService auditLogService;
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

    // 회원 단건 조회
    public Member findMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
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
        String rawPassword = request.getPassword();
        if (rawPassword.length() < 4 || rawPassword.length() > 20) {
            throw new RuntimeException("비밀번호는 4~20글자 사이여야 합니다.");
        }
        // 비밀번호 암호화 적용
        String encodedPassword =  passwordEncoder.encode(rawPassword);

        // 이름 유효성 검사
        String name = request.getName();
        if (!Pattern.matches("^[a-zA-Z가-힣]{2,10}$", name)) {
            throw new RuntimeException("이름은 한글과 알파벳만 가능하며, 2~10글자여야 합니다.");
        }

        // 회원 객체 생성 및 저장
        Member member = Member.builder()
                .memberId(memberId)
                .password(encodedPassword)
                .name(name)
                .role(Role.USER)
                // DTO에서 꺼내서 저장
                .companyName(request.getCompanyName())
                // 🚑 [수술 완료] 누락되었던 회사 전화번호 저장 로직 추가!
                .companyPhone(request.getCompanyPhone())
                .contact(request.getContact())
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
                .map(existing ->{
                    if(patch.getPassword() != null) {
                        String encodedNewPassword = passwordEncoder.encode(patch.getPassword());
                        existing.setPassword(encodedNewPassword);
                    }
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

    // 회원 정보 수정 (관리자)
    @Transactional
    public void updateMemberByAdmin(String memberId,
                                    MemberAdminUpdateRequest updateRequest,
                                    String adminActorId) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("수정하려는 회원이 존재하지 않습니다.: " + memberId));

        String oldRole = member.getRole().name();
        String oldName = member.getName();

        // Entity 업데이트 메서드 호출
        member.updateAdminInfo(
                updateRequest.getName(),
                updateRequest.getEmail(),
                updateRequest.getContact(),
                updateRequest.getCompanyName(),
                updateRequest.getCompanyPhone(),
                updateRequest.getRole()
        );

        String logDescription = String.format(
                "회원 [%s (%s)] 정보 수정 by [%s]: 이름 (%s -> %s), 역할 (%s -> %s)",
                memberId, member.getName(), adminActorId, oldName, updateRequest.getName(), oldRole, updateRequest.getRole().name()
        );
        auditLogService.saveLog(adminActorId, logDescription, LogType.MEMBER_MANAGEMENT);
    }

    // 회원 삭제 (회원 본인)
    public void deleteMemberWithPassword(String memberId, String password) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        if(!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        memberRepository.delete(member);
    }

    // 회원 삭제 (관리자가 회원)
    @Transactional
    public void deleteMember(String memberId, String adminActorId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 회원이 존재하지 않습니다."));

        memberRepository.delete(member);

        String actionDescription = "회원 [" + member.getName() + " (" + memberId + ")] 삭제 처리.";
        auditLogService.saveLog(adminActorId, actionDescription, LogType.MEMBER_MANAGEMENT);
    }
}