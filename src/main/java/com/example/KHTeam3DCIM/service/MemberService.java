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
import org.springframework.web.multipart.MultipartFile; // ⭐️ 파일 업로드를 위해 추가됨

import java.io.File;        // ⭐️ 추가됨
import java.io.IOException; // ⭐️ 추가됨
import java.util.List;
import java.util.UUID;      // ⭐️ 추가됨
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    // ⭐️ 파일 저장 경로 (프로젝트 폴더 내 uploads)
    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    private String maskString(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String firstChar = input.substring(0, 1);
        return firstChar + "**";
    }

    public Member findMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
    }

    // 1. 회원 전체 조회
    @Transactional(readOnly = true)
    public List<MemberResponse> findAllMembersUser() {
        return memberRepository.findAll()
                .stream()
                .map(m -> MemberResponse.builder()
                        .name(maskString(m.getName()))
                        .companyName(maskString(m.getCompanyName()))
                        .build())
                .collect(Collectors.toList());
    }

    // 회원가입 유효성 검사 (기존 로직 유지)
    private void validateMemberCreate(MemberCreateRequest request) {
        final String PHONE_PATTERN = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
        final String EMAIL_PATTERN = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

        if (request.getMemberId() == null || request.getMemberId().isBlank()) throw new IllegalArgumentException("아이디는 필수입니다.");
        if (request.getPassword() == null || request.getPassword().isBlank()) throw new IllegalArgumentException("비밀번호는 필수입니다.");
        if (request.getName() == null || request.getName().isBlank()) throw new IllegalArgumentException("담당자 성함은 필수입니다.");
        if (request.getCompanyName() == null || request.getCompanyName().isBlank()) throw new IllegalArgumentException("회사명은 필수입니다.");
        if (request.getContact() == null || request.getContact().isBlank()) throw new IllegalArgumentException("담당자 번호는 필수입니다.");
        if (request.getEmail() == null || request.getEmail().isBlank()) throw new IllegalArgumentException("담당자 이메일은 필수입니다.");

        if (!Pattern.matches("^[a-zA-Z0-9]{4,20}$", request.getMemberId())) throw new IllegalArgumentException("아이디 형식 오류");
        if (!Pattern.matches("^(?=.*[a-zA-Z])(?=.*\\d).{5,20}$", request.getPassword())) throw new IllegalArgumentException("비밀번호 형식 오류");
        if (!Pattern.matches("^[a-zA-Z가-힣]{2,10}$", request.getName())) throw new IllegalArgumentException("이름 형식 오류");
        if (!Pattern.matches("^[a-zA-Z0-9가-힣\\s]{2,30}$", request.getCompanyName())) throw new IllegalArgumentException("회사명 형식 오류");
        if (!Pattern.matches(PHONE_PATTERN, request.getContact())) throw new IllegalArgumentException("번호 형식 오류");
        if (!Pattern.matches(EMAIL_PATTERN, request.getEmail())) throw new IllegalArgumentException("이메일 형식 오류");
        if (!Pattern.matches(PHONE_PATTERN, request.getCompanyPhone())) throw new IllegalArgumentException("회사 번호 형식 오류");

        if (memberRepository.existsByMemberId(request.getMemberId())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }
    }

    @Transactional
    public MemberResponse addMember(MemberCreateRequest request) {
        validateMemberCreate(request);

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Member member = Member.builder()
                .memberId(request.getMemberId())
                .password(encodedPassword)
                .name(request.getName())
                .role(Role.USER)
                .companyName(request.getCompanyName())
                .companyPhone(request.getCompanyPhone())
                .contact(request.getContact())
                .email(request.getEmail())
                .build();

        Member saved = memberRepository.save(member);

        // 로그 내용 보완 (회사명 등 추가 가능)
        String logDescription = String.format("신규 회원가입 완료: %s (%s)", saved.getMemberId(), saved.getCompanyName());
        auditLogService.saveLog(saved.getMemberId(), logDescription, LogType.MEMBER_MANAGEMENT);

        return MemberResponse.builder().name(saved.getName()).build();
    }

    public boolean isMemberIdAvailable(String memberId) {
        if (memberId == null || !Pattern.matches("^[a-zA-Z0-9]{4,20}$", memberId)) return false;
        return !memberRepository.existsByMemberId(memberId);
    }
    // 회원 단일 조회
    public Member findByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
    }



    // ⭐️ 회원 정보 수정 (이미지 업로드 추가 & IOException 처리) ⭐️
    // 'throws IOException'이 있어야 Controller에서 catch를 할 수 있습니다.
    @Transactional
    public void updateMember(String memberId, MemberUpdateRequest request) throws IOException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + memberId));

        // 1. 비밀번호 수정
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            member.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        }

        // 2. 기본 정보 수정
        member.setEmail(request.getEmail());
        member.setContact(request.getContact());
        member.setCompanyName(request.getCompanyName());
        member.setCompanyPhone(request.getCompanyPhone());

        // 3. ⭐️ 프로필 이미지 저장 로직 ⭐️
        MultipartFile file = request.getProfileImage();
        if (file != null && !file.isEmpty()) {
            // 폴더 생성
            File folder = new File(uploadDir);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // 파일명 중복 방지 (UUID 사용)
            String originalFileName = file.getOriginalFilename();
            String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName;

            // 파일 저장 (여기서 IOException 발생 가능)
            File saveFile = new File(uploadDir + savedFileName);
            file.transferTo(saveFile);

            // DB에 파일명 저장
            member.setProfileImage(savedFileName);
        }
    }

    @Transactional
    public void updateMemberByAdmin(String memberId, MemberAdminUpdateRequest updateRequest, String adminActorId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("수정하려는 회원이 존재하지 않습니다.: " + memberId));

        String oldRole = member.getRole().name();
        String oldName = member.getName();

        member.updateAdminInfo(
                updateRequest.getName(),
                updateRequest.getEmail(),
                updateRequest.getContact(),
                updateRequest.getCompanyName(),
                updateRequest.getCompanyPhone(),
                updateRequest.getRole()
        );

        String logDescription = String.format("회원 [%s] 정보 수정 by [%s]", memberId, adminActorId);
        auditLogService.saveLog(adminActorId, logDescription, LogType.MEMBER_MANAGEMENT);
    }

    @Transactional
    public void deleteMemberWithPassword(String memberId, String password) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("회원 없음"));
        if(!passwordEncoder.matches(password, member.getPassword())) throw new RuntimeException("비밀번호 불일치");
        // 1. 로그 기록 (삭제되기 전에 수행)
        String logDescription = String.format("회원 본인 탈퇴 처리 (ID: %s, 이름: %s)", memberId, member.getName());
        auditLogService.saveLog(memberId, logDescription, LogType.MEMBER_MANAGEMENT);

        // 2. 회원 삭제
        memberRepository.delete(member);
    }

    @Transactional
    public void deleteMember(String memberId, String adminActorId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("회원 없음"));
        memberRepository.delete(member);
        auditLogService.saveLog(adminActorId, "회원 삭제: " + memberId, LogType.MEMBER_MANAGEMENT);
    }

    @Transactional
    public void resetPassword(MemberPasswordResetRequest request) {
        Member member = memberRepository.findById(request.getMemberId()).orElseThrow(() -> new IllegalArgumentException("아이디 없음"));
        if (!member.getName().equals(request.getName()) || !member.getEmail().equals(request.getEmail())) {
            throw new IllegalArgumentException("정보 불일치");
        }
        member.setPassword(passwordEncoder.encode(request.getNewPassword()));
        auditLogService.saveLog(member.getMemberId(), "비밀번호 재설정", LogType.MEMBER_MANAGEMENT);
    }
}