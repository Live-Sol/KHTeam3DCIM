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

// MemberService.java 전체 (필요한 부분만 발췌).

    // ⭐️ [정리] maskString 헬퍼 메서드는 클래스 내부에 한 번만 정의합니다. ⭐️
    private String maskString(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String firstChar = input.substring(0, 1);
        return firstChar + "**";
    }
    // 컨트롤러에서 엔티티 정보를 가져와 폼에 채우기 위한 메서드 (기존 findById 메서드 활용)
    public Member findMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
    }

    // 1. 회원 전체 조회 (회원용 - 이름, 회사명, role 표기 및 마스킹)
    @Transactional(readOnly = true)
    public List<MemberResponse> findAllMembersUser() {
        return memberRepository.findAll()
                .stream()
                .map(m -> MemberResponse.builder()
                        // 이름 마스킹 적용
                        .name(maskString(m.getName()))
                        // 회사명 마스킹 적용
                        .companyName(maskString(m.getCompanyName()))
                        .build())
                .collect(Collectors.toList());
    }

    // 회원 등록
    private void validateMemberCreate(MemberCreateRequest request) {
        // 공통 패턴
        final String PHONE_PATTERN = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
        final String EMAIL_PATTERN = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"; // 클라이언트 패턴보다 조금 더 상세함

        // 1. 필수 필드 null/blank 체크 (아이디, 비밀번호, 이름, 회사명)
        if (request.getMemberId() == null || request.getMemberId().isBlank()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("담당자 성함은 필수입니다.");
        }
        if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("회사명은 필수입니다.");
        }
        if (request.getContact() == null || request.getContact().isBlank()) {
            throw new IllegalArgumentException("담당자 번호는 필수입니다."); // <--- 추가
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("담당자 이메일은 필수입니다."); // <--- 추가
        }
        if (request.getCompanyPhone() == null || request.getCompanyPhone().isBlank()) {
            // 필수 여부에 따라 추가
        }


        // 2. 형식 (패턴) 체크
        if (!Pattern.matches("^[a-zA-Z0-9]{4,20}$", request.getMemberId())) {
            throw new IllegalArgumentException("아이디 형식 오류: 영문자와 숫자 4~20자");
        }
        if (!Pattern.matches("^(?=.*[a-zA-Z])(?=.*\\d).{5,20}$", request.getPassword())) {
            throw new IllegalArgumentException("비밀번호 형식 오류: 영문자/숫자 포함 5~20자");
        }
        if (!Pattern.matches("^[a-zA-Z가-힣]{2,10}$", request.getName())) {
            throw new IllegalArgumentException("이름 형식 오류: 한글 또는 알파벳 2~10자");
        }
        if (!Pattern.matches("^[a-zA-Z0-9가-힣\\s]{2,30}$", request.getCompanyName())) {
            throw new IllegalArgumentException("회사명 형식 오류: 한글, 영문, 숫자 2~30자"); // <--- 추가
        }
        if (!Pattern.matches(PHONE_PATTERN, request.getContact())) {
            throw new IllegalArgumentException("담당자 번호 형식 오류: 000-0000-0000"); // <--- 추가
        }
        if (!Pattern.matches(EMAIL_PATTERN, request.getEmail())) {
            throw new IllegalArgumentException("이메일 형식 오류"); // <--- 추가
        }
        if (!Pattern.matches(PHONE_PATTERN, request.getCompanyPhone())) {
            throw new IllegalArgumentException("회사 대표 번호 형식 오류: 000-0000-0000"); // <--- 추가
        }

        // 3. 중복 체크 (DB 접근)
        if (memberRepository.existsByMemberId(request.getMemberId())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }
        // 이메일도 중복 체크를 할지 결정해야 함
    }
    // addMember 메서드에서는 DataIntegrityViolationException try-catch를 제거하는 것을 권장합니다.
    @Transactional
    public MemberResponse addMember(MemberCreateRequest request) {
        validateMemberCreate(request); // 여기서 중복 검사를 확실히 끝냅니다.

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Member member = Member.builder()
                .memberId(request.getMemberId())
                .password(encodedPassword)
                .name(request.getName())
                .role(Role.USER)
                .companyName(request.getCompanyName())
                .companyPhone(request.getCompanyPhone())
                .contact(request.getContact())
                .email(request.getEmail()) // DTO에 email이 있다면 추가해야 함
                .build();

        Member saved = memberRepository.save(member);
        // ⭐️ 회원가입 성공 후 로그 기록 추가 ⭐️
        String actionDescription = String.format("새 회원 가입 완료. ID: [%s], 이름: [%s]",
                saved.getMemberId(), saved.getName());
        // 행위자(actorId)는 방금 가입한 본인 ID가 됩니다.
        auditLogService.saveLog(saved.getMemberId(), actionDescription, LogType.MEMBER_MANAGEMENT);

        return MemberResponse.builder()
                .name(saved.getName())
                // 필요한 경우 memberId, id 등을 추가
                .build();
    }
    // 아이디 중복 검사
    public boolean isMemberIdAvailable(String memberId) {
        // MemberId가 Null이거나 형식에 맞지 않는 경우를 대비한 검증 로직을 추가하는 것이 좋습니다.
        if (memberId == null || !Pattern.matches("^[a-zA-Z0-9]{4,20}$", memberId)) {
            // 유효하지 않은 형식은 사용 불가능으로 처리
            return false;
        }

        // memberRepository가 주입되어 있어야 함 (DI 확인)
        return !memberRepository.existsByMemberId(memberId);
    }

    // ️ 회원 정보 수정 (USER 권한)
    @Transactional
    public void updateMember(String memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + memberId));

        // 1. 비밀번호 수정 (새 비밀번호가 입력된 경우에만)
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            member.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        }

        // 2. 기타 수정 가능한 필드 업데이트
        // 아이디, 이름, Role은 엔티티의 기존 값을 유지합니다.
        member.setEmail(request.getEmail());
        member.setContact(request.getContact());
        member.setCompanyName(request.getCompanyName());
        member.setCompanyPhone(request.getCompanyPhone());

        // @Transactional에 의해 트랜잭션 종료 시 변경된 내용이 DB에 자동 반영됩니다.
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

    @Transactional
    public void resetPassword(MemberPasswordResetRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 아이디입니다."));

        // 본인 확인 (이름과 이메일이 일치하는지 검사)
        if (!member.getName().equals(request.getName()) ||
                !member.getEmail().equals(request.getEmail())) {
            throw new IllegalArgumentException("회원 정보가 일치하지 않습니다. (이름 또는 이메일 확인 필요)");
        }

        // 새 비밀번호 암호화 및 저장
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        member.setPassword(encodedPassword);

        // 감사 로그 기록
        auditLogService.saveLog(member.getMemberId(), "비밀번호 재설정(Account Recovery) 완료", LogType.MEMBER_MANAGEMENT);
    }
}