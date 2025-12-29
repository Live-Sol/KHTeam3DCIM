package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.domain.LogType;
import com.example.KHTeam3DCIM.dto.admin.MemberAdminResponse;
import com.example.KHTeam3DCIM.dto.admin.MemberAdminUpdateRequest;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final MemberRepository memberRepository;
    private final AuditLogService auditLogService;
    // PasswordEncoder는 관리자 수정/삭제 시에는 사용하지 않으므로 제거합니다.

    // (1-1) 전체 회원 조회 (관리자용 - 모든 정보 표기)
    @Transactional(readOnly = true)
    public List<MemberAdminResponse> findAllMembersAdmin() {
        // 전체 회원을 Role을 기준으로 오름차순(Asc) 정렬하여 조회
        return memberRepository.findAllByOrderByRoleAsc()
                .stream()
                .map(m -> MemberAdminResponse.builder()
                        .memberId(m.getMemberId())
                        .name(m.getName())
                        .email(m.getEmail())
                        .contact(m.getContact())
                        .companyName(m.getCompanyName())
                        .companyPhone(m.getCompanyPhone())
                        .role(m.getRole())
                        .build())
                .collect(Collectors.toList());
    }


    // 관리자용 특정 아이디로 회원 조회 (마스킹 없이 모든 정보 노출)
    @Transactional(readOnly = true)
    public List<MemberAdminResponse> findMembersByMemberIdAdmin(String memberId) {

        // Repository의 findByMemberIdLike 호출
        List<Member> foundMembers = memberRepository.findByMemberIdLike(memberId);

        // 마스킹 없이 MemberAdminResponse DTO로 변환하여 반환
        return foundMembers.stream()
                .map(m -> MemberAdminResponse.builder()
                        .memberId(m.getMemberId())
                        .name(m.getName())
                        .email(m.getEmail())
                        .contact(m.getContact())
                        .companyName(m.getCompanyName())
                        .companyPhone(m.getCompanyPhone())
                        .role(m.getRole())
                        .build())
                .collect(Collectors.toList());
    }

    // 회원 단건 조회 (관리자 수정 폼 로딩용)
    @Transactional(readOnly = true)
    public Member findMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
    }

    // 회원 정보 수정 (관리자)
    @Transactional
    public void updateMemberByAdmin(String memberId,
                                    MemberAdminUpdateRequest updateRequest,
                                    String adminActorId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        // 1. 변경 전 상태 저장 (로그 기록)
        String oldRole = member.getRole().name();
        String oldName = member.getName();
        String oldEmail = member.getEmail();
        String oldContact = member.getContact();

        // 2. 엔티티 업데이트 (Member.java의 updateAdminInfo 메서드 사용.)
        member.updateAdminInfo(
                updateRequest.getName(),
                updateRequest.getEmail(),
                updateRequest.getContact(),
                updateRequest.getCompanyName(),
                updateRequest.getCompanyPhone(),
                updateRequest.getRole()
        );

        // 3. 로그 기록
        String logDescription = String.format(
                "회원 [%s (%s)] 정보 수정 by [%s]: 이름 (%s -> %s), 권한 (%s -> %s), " +
                        "이메일 (%s -> %s), 연락처 (%s -> %s)",
                memberId, member.getName(), adminActorId,
                oldName, updateRequest.getName(),
                oldRole, updateRequest.getRole().name(),
                oldEmail, updateRequest.getEmail(),
                oldContact, updateRequest.getContact()
        );
        auditLogService.saveLog(adminActorId, logDescription, LogType.MEMBER_MANAGEMENT);
    }

    // 회원 삭제 (관리자가 회원)
    public void deleteMember(String memberId, String adminActorId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 회원이 존재하지 않습니다."));

        // * 중요: ADMIN 역할 계정을 삭제하려고 시도하는지 재검사 (보안 강화)
        if (member.getRole() == com.example.KHTeam3DCIM.domain.Role.ADMIN) {
            throw new RuntimeException("관리자 계정은 삭제할 수 없습니다.");
        }
        memberRepository.delete(member);

        // 로그 기록
        String actionDescription = "회원 [" + member.getName() + " (" + memberId + ")] 삭제 처리.";
        auditLogService.saveLog(adminActorId, actionDescription, LogType.MEMBER_MANAGEMENT);
    }
}