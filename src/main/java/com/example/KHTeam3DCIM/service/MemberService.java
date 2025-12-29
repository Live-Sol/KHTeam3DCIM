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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    // 파일 저장 경로
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

    private void validateMemberCreate(MemberCreateRequest request) {
        final String PHONE_PATTERN = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
        final String EMAIL_PATTERN = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

        if (request.getMemberId() == null || request.getMemberId().isBlank()) throw new IllegalArgumentException("아이디는 필수입니다.");
        if (request.getPassword() == null || request.getPassword().isBlank()) throw new IllegalArgumentException("비밀번호는 필수입니다.");
        // ... (나머지 유효성 검사 로직 유지)
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
        String logDescription = String.format("신규 회원가입 완료: %s (%s)", saved.getMemberId(), saved.getCompanyName());
        auditLogService.saveLog(saved.getMemberId(), logDescription, LogType.MEMBER_MANAGEMENT);

        return MemberResponse.builder().name(saved.getName()).build();
    }

    public boolean isMemberIdAvailable(String memberId) {
        if (memberId == null || !Pattern.matches("^[a-zA-Z0-9]{4,20}$", memberId)) return false;
        return !memberRepository.existsByMemberId(memberId);
    }

    public Member findByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
    }

    @Transactional
    public void updateMember(String memberId, MemberUpdateRequest request) throws IOException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + memberId));

        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            member.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        }

        member.setEmail(request.getEmail());
        member.setContact(request.getContact());
        member.setCompanyName(request.getCompanyName());
        member.setCompanyPhone(request.getCompanyPhone());

        MultipartFile newFile = request.getProfileImage();
        boolean isDeleteRequested = Boolean.TRUE.equals(request.getDeleteProfileImage());
        boolean isNewFileUploaded = (newFile != null && !newFile.isEmpty());

        if (isDeleteRequested || isNewFileUploaded) {
            if (member.getProfileImage() != null) {
                File oldFile = new File(uploadDir + member.getProfileImage());
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
        }

        if (isDeleteRequested) {
            member.setProfileImage(null);
        }

        if (isNewFileUploaded) {
            File folder = new File(uploadDir);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String originalFileName = newFile.getOriginalFilename();
            String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
            File saveFile = new File(uploadDir + savedFileName);
            newFile.transferTo(saveFile);
            member.setProfileImage(savedFileName);
        }
    }

    // [수정된 권장 로직] 회원 탈퇴 (Soft Delete + 익명화)
    @Transactional
    public void withdrawMember(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        // 1. 개인정보 파기 (개인정보 보호법 준수 및 보안 강화)
        // 이름, 이메일, 연락처 등을 식별 불가능한 값으로 덮어씁니다.
        // memberId는 장비 테이블과의 연결 고리(FK)이므로 유지하거나, 필요 시 별도 처리가 필요하지만 보통 유지합니다.
        member.setName("탈퇴회원");
        member.setEmail(UUID.randomUUID().toString().substring(0, 8) + "@deleted.user"); // 중복 방지를 위한 난수 이메일
        member.setContact("000-0000-0000");
        member.setCompanyName("Unknown");
        member.setCompanyPhone("");

        // 2. 비밀번호 파기 (로그인 원천 차단)
        member.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // 임의의 값으로 변경

        // 3. 프로필 이미지 삭제 (파일도 정리)
        if (member.getProfileImage() != null) {
            File file = new File(uploadDir + member.getProfileImage());
            if (file.exists()) {
                file.delete();
            }
            member.setProfileImage(null);
        }

        // 4. 상태 변경 (Soft Delete)
        member.setDeleted(true);

        // 5. 로그 기록
        auditLogService.saveLog(memberId, "회원 탈퇴(익명화 처리 완료)", LogType.MEMBER_MANAGEMENT);
    }

    // 관리자용 수정
    @Transactional
    public void updateMemberByAdmin(String memberId, MemberAdminUpdateRequest updateRequest, String adminActorId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("수정하려는 회원이 존재하지 않습니다.: " + memberId));

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

    // 기존 비밀번호 검증 후 하드 삭제 로직 (사용 안 함, 혹시 몰라 유지)
    @Transactional
    public void deleteMemberWithPassword(String memberId, String password) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("회원 없음"));
        if(!passwordEncoder.matches(password, member.getPassword())) throw new RuntimeException("비밀번호 불일치");

        auditLogService.saveLog(memberId, "회원 본인 삭제", LogType.MEMBER_MANAGEMENT);
        memberRepository.delete(member); // Hard Delete
    }

    // 관리자용 강제 삭제
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